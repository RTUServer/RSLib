package io.papermc.paper.plugin.entrypoint.strategy;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.mojang.logging.LogUtils;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.entrypoint.dependency.DependencyUtil;
import io.papermc.paper.plugin.entrypoint.dependency.GraphDependencyContext;
import io.papermc.paper.plugin.provider.PluginProvider;
import io.papermc.paper.plugin.provider.configuration.LoadOrderConfiguration;
import io.papermc.paper.plugin.provider.configuration.PaperPluginMeta;
import io.papermc.paper.plugin.provider.type.spigot.SpigotPluginProvider;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.plugin.UnknownDependencyException;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class ModernPluginLoadingStrategy<T> implements ProviderLoadingStrategy<T> {

    private static final Logger LOGGER = LogUtils.getClassLogger();
    private final ProviderConfiguration<T> configuration;

    public ModernPluginLoadingStrategy(ProviderConfiguration<T> onLoad) {
        this.configuration = onLoad;
    }

    @Override
    public List<ProviderPair<T>> loadProviders(List<PluginProvider<T>> pluginProviders) {
        Map<String, PluginProviderEntry<T>> providerMap = new HashMap<>();
        Map<String, PluginProvider<?>> providerMapMirror = Maps.transformValues(providerMap, (entry) -> entry.provider);
        List<PluginProvider<T>> validatedProviders = new ArrayList<>();

        // Populate provider map
        for (PluginProvider<T> provider : pluginProviders) {
            PluginMeta providerConfig = provider.getMeta();
            PluginProviderEntry<T> entry = new PluginProviderEntry<>(provider);

            PluginProviderEntry<T> replacedProvider = providerMap.put(providerConfig.getName(), entry);
            if (replacedProvider != null) {
                LOGGER.error(String.format(
                    "Ambiguous plugin name '%s' for files '%s' and '%s' in '%s'",
                    providerConfig.getName(),
                    provider.getSource(),
                    replacedProvider.provider.getSource(),
                    replacedProvider.provider.getParentSource()
                ));
            }

            for (String extra : providerConfig.getProvidedPlugins()) {
                PluginProviderEntry<T> replacedExtraProvider = providerMap.putIfAbsent(extra, entry);
                if (replacedExtraProvider != null) {
                    LOGGER.warn(String.format(
                        "`%s' is provided by both `%s' and `%s'",
                        extra,
                        providerConfig.getName(),
                        replacedExtraProvider.provider.getMeta().getName()
                    ));
                }
            }
        }

        // Validate providers, ensuring all of them have valid dependencies. Removing those who are invalid
        for (PluginProvider<T> provider : pluginProviders) {
            PluginMeta configuration = provider.getMeta();

            // Populate missing dependencies to capture if there are multiple missing ones.
            List<String> missingDependencies = provider.validateDependencies(providerMapMirror);

            if (missingDependencies.isEmpty()) {
                validatedProviders.add(provider);
            } else {
                LOGGER.error("Could not load '%s' in '%s'".formatted(provider.getSource(), provider.getParentSource()), new UnknownDependencyException(missingDependencies, configuration.getName())); // Paper
                // Because the validator is invalid, remove it from the provider map
                providerMap.remove(configuration.getName());
            }
        }

        MutableGraph<String> loadOrderGraph = GraphBuilder.directed().build();
        MutableGraph<String> dependencyGraph = GraphBuilder.directed().build();
        for (PluginProvider<?> validated : validatedProviders) {
            PluginMeta configuration = validated.getMeta();
            LoadOrderConfiguration loadOrderConfiguration = validated.createConfiguration(providerMapMirror);

            // Build a validated provider's load order changes
            DependencyUtil.buildLoadGraph(loadOrderGraph, loadOrderConfiguration, providerMap::containsKey);

            // Build a validated provider's dependencies into the graph
            DependencyUtil.buildDependencyGraph(dependencyGraph, configuration);

            // Add the provided plugins to the graph as well
            for (String provides : configuration.getProvidedPlugins()) {
                String name = configuration.getName();
                DependencyUtil.addProvidedPlugin(loadOrderGraph, name, provides);
                DependencyUtil.addProvidedPlugin(dependencyGraph, name, provides);
            }
        }

        // Reverse the topographic search to let us see which providers we can load first.
        List<String> reversedTopographicSort;
        try {
            reversedTopographicSort = Lists.reverse(TopographicGraphSorter.sortGraph(loadOrderGraph));
        } catch (TopographicGraphSorter.GraphCycleException exception) {
            List<List<String>> cycles = new JohnsonSimpleCycles<>(loadOrderGraph).findAndRemoveSimpleCycles();

            // Only log an error if at least non-Spigot plugin is present in the cycle
            // Due to Spigot plugin metadata making no distinction between load order and dependencies (= class loader access), cycles are an unfortunate reality we have to deal with
            Set<String> cyclingPlugins = new HashSet<>();
            cycles.forEach(cyclingPlugins::addAll);
            if (cyclingPlugins.stream().anyMatch(plugin -> {
                PluginProvider<?> pluginProvider = providerMapMirror.get(plugin);
                return pluginProvider != null && !(pluginProvider instanceof SpigotPluginProvider);
            })) {
                logCycleError(cycles, providerMapMirror);
            }

            // Try again after hopefully having removed all cycles
            try {
                reversedTopographicSort = Lists.reverse(TopographicGraphSorter.sortGraph(loadOrderGraph));
            } catch (TopographicGraphSorter.GraphCycleException e) {
                throw new PluginGraphCycleException(cycles);
            }
        }

        GraphDependencyContext graphDependencyContext = new GraphDependencyContext(dependencyGraph);
        List<ProviderPair<T>> loadedPlugins = new ArrayList<>();
        for (String providerIdentifier : reversedTopographicSort) {
            // It's possible that this will be null because the above dependencies for soft/load before aren't validated if they exist.
            // The graph could be MutableGraph<PluginProvider<T>>, but we would have to check if each dependency exists there... just
            // nicer to do it here TBH.
            PluginProviderEntry<T> retrievedProviderEntry = providerMap.get(providerIdentifier);
            if (retrievedProviderEntry == null || retrievedProviderEntry.provided) {
                // OR if this was already provided (most likely from a plugin that already "provides" that dependency)
                // This won't matter since the provided plugin is loaded as a dependency, meaning it should have been loaded correctly anyways
                continue; // Skip provider that doesn't exist....
            }
            retrievedProviderEntry.provided = true;
            PluginProvider<T> retrievedProvider = retrievedProviderEntry.provider;
            try {
                this.configuration.applyContext(retrievedProvider, graphDependencyContext);

                if (this.configuration.preloadProvider(retrievedProvider)) {
                    T instance = retrievedProvider.createInstance();
                    if (this.configuration.load(retrievedProvider, instance)) {
                        loadedPlugins.add(new ProviderPair<>(retrievedProvider, instance));
                    }
                }
            } catch (Throwable ex) {
                LOGGER.error("Could not load plugin '%s' in folder '%s'".formatted(retrievedProvider.getFileName(), retrievedProvider.getParentSource()), ex); // Paper
            }
        }

        return loadedPlugins;
    }

    private void logCycleError(List<List<String>> cycles, Map<String, PluginProvider<?>> providerMapMirror) {
        LOGGER.error("=================================");
        LOGGER.error("Circular plugin loading detected:");
        for (int i = 0; i < cycles.size(); i++) {
            List<String> cycle = cycles.get(i);
            LOGGER.error("{}) {} -> {}", i + 1, String.join(" -> ", cycle), cycle.get(0));
            for (String pluginName : cycle) {
                PluginProvider<?> pluginProvider = providerMapMirror.get(pluginName);
                if (pluginProvider == null) {
                    return;
                }

                logPluginInfo(pluginProvider.getMeta());
            }
        }

        LOGGER.error("Please report this to the plugin authors of the first plugin of each loop or join the PaperMC Discord server for further help.");
        LOGGER.error("=================================");
    }

    private void logPluginInfo(PluginMeta meta) {
        if (!meta.getLoadBeforePlugins().isEmpty()) {
            LOGGER.error("   {} loadbefore: {}", meta.getName(), meta.getLoadBeforePlugins());
        }

        if (meta instanceof PaperPluginMeta paperPluginMeta) {
            if (!paperPluginMeta.getLoadAfterPlugins().isEmpty()) {
                LOGGER.error("   {} loadafter: {}", meta.getName(), paperPluginMeta.getLoadAfterPlugins());
            }
        } else {
            List<String> dependencies = new ArrayList<>();
            dependencies.addAll(meta.getPluginDependencies());
            dependencies.addAll(meta.getPluginSoftDependencies());
            if (!dependencies.isEmpty()) {
                LOGGER.error("   {} depend/softdepend: {}", meta.getName(), dependencies);
            }
        }
    }

    private static class PluginProviderEntry<T> {

        private final PluginProvider<T> provider;
        private boolean provided;

        private PluginProviderEntry(PluginProvider<T> provider) {
            this.provider = provider;
        }
    }
}
