package io.papermc.paper.plugin.bootstrap;

import io.papermc.paper.plugin.PluginInitializerManager;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.provider.PluginProvider;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public record PluginProviderContextImpl(PluginMeta config, Path dataFolder,
                                        ComponentLogger logger) implements PluginProviderContext {

    public static PluginProviderContextImpl of(PluginMeta config, ComponentLogger logger) {
        Path dataFolder = PluginInitializerManager.instance().pluginDirectoryPath().resolve(config.getDisplayName());

        return new PluginProviderContextImpl(config, dataFolder, logger);
    }

    public static PluginProviderContextImpl of(PluginProvider<?> provider, Path pluginFolder) {
        Path dataFolder = pluginFolder.resolve(provider.getMeta().getDisplayName());

        return new PluginProviderContextImpl(provider.getMeta(), dataFolder, provider.getLogger());
    }

    @Override
    public @NotNull PluginMeta getConfiguration() {
        return this.config;
    }

    @Override
    public @NotNull Path getDataDirectory() {
        return this.dataFolder;
    }

    @Override
    public @NotNull ComponentLogger getLogger() {
        return this.logger;
    }
}
