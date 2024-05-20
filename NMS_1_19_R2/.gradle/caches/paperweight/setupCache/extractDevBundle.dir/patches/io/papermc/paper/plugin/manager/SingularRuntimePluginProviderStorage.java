package io.papermc.paper.plugin.manager;

import com.destroystokyo.paper.util.SneakyThrow;
import io.papermc.paper.plugin.entrypoint.Entrypoint;
import io.papermc.paper.plugin.entrypoint.LaunchEntryPointHandler;
import io.papermc.paper.plugin.provider.PluginProvider;
import io.papermc.paper.plugin.provider.type.paper.PaperPluginParent;
import io.papermc.paper.plugin.storage.ServerPluginProviderStorage;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Used for registering a single plugin provider.
 * This has special behavior in that some errors are thrown instead of logged.
 */
class SingularRuntimePluginProviderStorage extends ServerPluginProviderStorage {

    private PluginProvider<JavaPlugin> lastProvider;
    private JavaPlugin singleLoaded;

    @Override
    public void register(PluginProvider<JavaPlugin> provider) {
        super.register(provider);
        if (this.lastProvider != null) {
            SneakyThrow.sneaky(new InvalidPluginException("Plugin registered two JavaPlugins"));
        }
        if (provider instanceof PaperPluginParent.PaperServerPluginProvider) {
            throw new IllegalStateException("Cannot register paper plugins during runtime!");
        }
        this.lastProvider = provider;
        // Register the provider into the server entrypoint, this allows it to show in /plugins correctly.
        // Generally it might be better in the future to make a separate storage, as putting it into the entrypoint handlers doesn't make much sense.
        LaunchEntryPointHandler.INSTANCE.register(Entrypoint.PLUGIN, provider);
    }

    @Override
    public void enter() {
        PluginProvider<JavaPlugin> provider = this.lastProvider;
        if (provider == null) {
            return;
        }

        // Manually validate dependencies, LEGACY BEHAVIOR.
        // Normally it is logged, but manually adding one plugin will cause it to actually throw exceptions.
        PluginDescriptionFile descriptionFile = (PluginDescriptionFile) provider.getMeta();
        List<String> missingDependencies = new ArrayList<>();
        for (String dependency : descriptionFile.getDepend()) {
            if (!PaperPluginManagerImpl.getInstance().isPluginEnabled(dependency)) {
                missingDependencies.add(dependency);
            }
        }
        if (!missingDependencies.isEmpty()) {
            throw new UnknownDependencyException(missingDependencies, provider.getFileName().toString());
        }

        // Go through normal plugin loading logic
        super.enter();
    }

    @Override
    public void processProvided(PluginProvider<JavaPlugin> provider, JavaPlugin provided) {
        super.processProvided(provider, provided);
        this.singleLoaded = provided;
    }

    @Override
    public boolean throwOnCycle() {
        return false;
    }

    public Optional<JavaPlugin> getSingleLoaded() {
        return Optional.ofNullable(this.singleLoaded);
    }
}
