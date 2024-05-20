package io.papermc.paper.plugin.storage;

import com.mojang.logging.LogUtils;
import io.papermc.paper.plugin.PluginInitializerManager;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.bootstrap.PluginProviderContextImpl;
import io.papermc.paper.plugin.provider.entrypoint.DependencyContext;
import io.papermc.paper.plugin.entrypoint.dependency.DependencyContextHolder;
import io.papermc.paper.plugin.entrypoint.strategy.ModernPluginLoadingStrategy;
import io.papermc.paper.plugin.entrypoint.strategy.PluginGraphCycleException;
import io.papermc.paper.plugin.entrypoint.strategy.ProviderConfiguration;
import io.papermc.paper.plugin.provider.PluginProvider;
import io.papermc.paper.plugin.provider.ProviderStatus;
import io.papermc.paper.plugin.provider.ProviderStatusHolder;
import io.papermc.paper.plugin.provider.configuration.PaperPluginMeta;
import io.papermc.paper.plugin.provider.configuration.type.DependencyConfiguration;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class BootstrapProviderStorage extends SimpleProviderStorage<PluginBootstrap> {

    private static final Logger LOGGER = LogUtils.getLogger();

    public BootstrapProviderStorage() {
        super(new ModernPluginLoadingStrategy<>(new ProviderConfiguration<>() {
            @Override
            public void applyContext(PluginProvider<PluginBootstrap> provider, DependencyContext dependencyContext) {
                if (provider instanceof DependencyContextHolder contextHolder) {
                    contextHolder.setContext(dependencyContext);
                }
            }

            @Override
            public boolean load(PluginProvider<PluginBootstrap> provider, PluginBootstrap provided) {
                try {
                    PluginProviderContext context = PluginProviderContextImpl.of(provider, PluginInitializerManager.instance().pluginDirectoryPath());
                    provided.bootstrap(context);
                    return true;
                } catch (Exception e) {
                    LOGGER.error("Failed to run bootstrapper for %s. This plugin will not be loaded.".formatted(provider.getSource()), e);
                    if (provider instanceof ProviderStatusHolder statusHolder) {
                        statusHolder.setStatus(ProviderStatus.ERRORED);
                    }
                    return false;
                }
            }
        }));
    }

    @Override
    public String toString() {
        return "BOOTSTRAP:" + super.toString();
    }
}
