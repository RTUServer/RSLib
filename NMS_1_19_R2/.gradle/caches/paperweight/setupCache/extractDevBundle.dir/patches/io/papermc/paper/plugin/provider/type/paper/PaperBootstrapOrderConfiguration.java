package io.papermc.paper.plugin.provider.type.paper;

import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.provider.configuration.LoadOrderConfiguration;
import io.papermc.paper.plugin.provider.configuration.PaperPluginMeta;
import io.papermc.paper.plugin.provider.configuration.type.LoadConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PaperBootstrapOrderConfiguration implements LoadOrderConfiguration {

    private final PaperPluginMeta paperPluginMeta;
    private final List<String> loadBefore = new ArrayList<>();
    private final List<String> loadAfter = new ArrayList<>();

    public PaperBootstrapOrderConfiguration(PaperPluginMeta paperPluginMeta) {
        this.paperPluginMeta = paperPluginMeta;

        for (LoadConfiguration configuration : paperPluginMeta.getLoadAfter()) {
            if (configuration.bootstrap()) {
                this.loadAfter.add(configuration.name());
            }
        }
        for (LoadConfiguration configuration : paperPluginMeta.getLoadBefore()) {
            if (configuration.bootstrap()) {
                this.loadBefore.add(configuration.name());
            }
        }
    }

    @Override
    public @NotNull List<String> getLoadBefore() {
        return this.loadBefore;
    }

    @Override
    public @NotNull List<String> getLoadAfter() {
        return this.loadAfter;
    }

    @Override
    public @NotNull PluginMeta getMeta() {
        return this.paperPluginMeta;
    }
}
