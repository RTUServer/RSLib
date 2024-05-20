package io.papermc.paper.plugin.entrypoint.dependency;

import com.google.common.graph.MutableGraph;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.provider.PluginProvider;
import io.papermc.paper.plugin.provider.configuration.LoadOrderConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@SuppressWarnings("UnstableApiUsage")
public class DependencyUtil {

    @NotNull
    public static MutableGraph<String> buildDependencyGraph(@NotNull MutableGraph<String> dependencyGraph, @NotNull PluginMeta configuration) {
        List<String> dependencies = new ArrayList<>();
        dependencies.addAll(configuration.getPluginDependencies());
        dependencies.addAll(configuration.getPluginSoftDependencies());

        return buildDependencyGraph(dependencyGraph, configuration.getName(), dependencies);
    }

    @NotNull
    public static MutableGraph<String> buildDependencyGraph(@NotNull MutableGraph<String> dependencyGraph, String identifier, @NotNull Iterable<String> depends) {
        for (String dependency : depends) {
            dependencyGraph.putEdge(identifier, dependency);
        }

        dependencyGraph.addNode(identifier); // Make sure dependencies at least have a node
        return dependencyGraph;
    }

    @NotNull
    public static MutableGraph<String> buildLoadGraph(@NotNull MutableGraph<String> dependencyGraph, @NotNull LoadOrderConfiguration configuration, Predicate<String> validator) {
        String identifier = configuration.getMeta().getName();
        for (String dependency : configuration.getLoadAfter()) {
            if (validator.test(dependency)) {
                dependencyGraph.putEdge(identifier, dependency);
            }
        }

        for (String loadBeforeTarget : configuration.getLoadBefore()) {
            if (validator.test(loadBeforeTarget)) {
                dependencyGraph.putEdge(loadBeforeTarget, identifier);
            }
        }

        dependencyGraph.addNode(identifier); // Make sure dependencies at least have a node
        return dependencyGraph;
    }

    // This adds a provided plugin to another plugin, basically making it seem like a "dependency"
    // in order to have plugins that need the provided plugin to load after the specified plugin name
    @NotNull
    public static MutableGraph<String> addProvidedPlugin(@NotNull MutableGraph<String> dependencyGraph, @NotNull String pluginName, @NotNull String providedName) {
        dependencyGraph.putEdge(pluginName, providedName);

        return dependencyGraph;
    }

    public static List<String> validateSimple(PluginMeta meta, Map<String, PluginProvider<?>> toLoad) {
        List<String> missingDependencies = new ArrayList<>();
        for (String hardDependency : meta.getPluginDependencies()) {
            if (!toLoad.containsKey(hardDependency)) {
                missingDependencies.add(hardDependency);
            }
        }

        return missingDependencies;
    }
}
