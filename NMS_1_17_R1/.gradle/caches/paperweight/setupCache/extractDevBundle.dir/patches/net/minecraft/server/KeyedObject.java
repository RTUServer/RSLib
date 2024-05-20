package net.minecraft.server;

import net.minecraft.resources.ResourceLocation;

// TODO(Mariell Hoversholm): Move stupid ass class
public interface KeyedObject {
    ResourceLocation getMinecraftKey();
    default String getMinecraftKeyString() {
        ResourceLocation key = getMinecraftKey();
        return key != null ? key.toString() : null;
    }
}
