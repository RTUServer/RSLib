package org.bukkit.craftbukkit.v1_17_R1.generator;

import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.StructureSettings;

// Do not implement functions to this class, add to NormalChunkGenerator
public abstract class InternalChunkGenerator extends net.minecraft.world.level.chunk.ChunkGenerator {

    public InternalChunkGenerator(BiomeSource biomeSource, StructureSettings structuresConfig) {
        super(biomeSource, structuresConfig);
    }
}
