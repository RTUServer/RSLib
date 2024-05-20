package io.papermc.paper.plugin.bootstrap;

import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.provider.PluginProvider;
import java.nio.file.Path;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;

public record PluginBootstrapContextImpl(PluginMeta config, Path dataFolder,
                                         ComponentLogger logger, Path pluginSource) implements BootstrapContext {

    public static PluginBootstrapContextImpl of(PluginProvider<?> provider, Path pluginFolder) {
        Path dataFolder = pluginFolder.resolve(provider.getMeta().getName());

        return new PluginBootstrapContextImpl(provider.getMeta(), dataFolder, provider.getLogger(), provider.getSource());
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

    @Override
    public @NotNull Path getPluginSource() {
        return this.pluginSource;
    }
}
