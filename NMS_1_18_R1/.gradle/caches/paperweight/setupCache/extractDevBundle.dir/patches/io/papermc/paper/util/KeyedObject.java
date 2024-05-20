package io.papermc.paper.util;

import net.minecraft.resources.ResourceLocation;

public interface KeyedObject {
    ResourceLocation getMinecraftKey();
    default String getMinecraftKeyString() {
        ResourceLocation key = getMinecraftKey();
        return key != null ? key.toString() : null;
    }
}
