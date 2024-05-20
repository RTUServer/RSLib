package io.papermc.paper.plugin.provider.configuration.type;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Required;

@ConfigSerializable
public record DependencyConfiguration(
    @Required String name,
    boolean required,
    boolean bootstrap
) {
}
