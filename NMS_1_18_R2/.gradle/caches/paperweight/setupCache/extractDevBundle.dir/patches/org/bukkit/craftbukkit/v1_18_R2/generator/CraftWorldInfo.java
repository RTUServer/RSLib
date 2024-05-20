package org.bukkit.craftbukkit.v1_18_R2.generator;

import java.util.UUID;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_18_R2.util.WorldUUID;
import org.bukkit.generator.WorldInfo;

public class CraftWorldInfo implements WorldInfo {

    private final String name;
    private final UUID uuid;
    private final World.Environment environment;
    private final long seed;
    private final int minHeight;
    private final int maxHeight;
    // Paper start
    private final net.minecraft.world.level.chunk.ChunkGenerator vanillaChunkGenerator;
    private final net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> biomeRegistry;

    public CraftWorldInfo(ServerLevelData worldDataServer, LevelStorageSource.LevelStorageAccess session, World.Environment environment, DimensionType dimensionManager) {
        this(worldDataServer, session, environment, dimensionManager, null, null);
    }
    public CraftWorldInfo(ServerLevelData worldDataServer, LevelStorageSource.LevelStorageAccess session, World.Environment environment, DimensionType dimensionManager, net.minecraft.world.level.chunk.ChunkGenerator chunkGenerator, net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> biomeRegistry) {
        this.biomeRegistry = biomeRegistry;
        this.vanillaChunkGenerator = chunkGenerator;
        // Paper end
        this.name = worldDataServer.getLevelName();
        this.uuid = WorldUUID.getUUID(session.levelPath.toFile());
        this.environment = environment;
        this.seed = ((PrimaryLevelData) worldDataServer).worldGenSettings().seed();
        this.minHeight = dimensionManager.minY();
        this.maxHeight = dimensionManager.minY() + dimensionManager.height();
    }

    public CraftWorldInfo(String name, UUID uuid, World.Environment environment, long seed, int minHeight, int maxHeight) {
        // Paper start
        this.vanillaChunkGenerator = null;
        this.biomeRegistry = null;
        // Paper end
        this.name = name;
        this.uuid = uuid;
        this.environment = environment;
        this.seed = seed;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public UUID getUID() {
        return this.uuid;
    }

    @Override
    public World.Environment getEnvironment() {
        return this.environment;
    }

    @Override
    public long getSeed() {
        return this.seed;
    }

    @Override
    public int getMinHeight() {
        return this.minHeight;
    }

    @Override
    public int getMaxHeight() {
        return this.maxHeight;
    }

    // Paper start
    @Override
    public org.bukkit.generator.BiomeProvider vanillaBiomeProvider() {
        final java.util.List<org.bukkit.block.Biome> possibleBiomes = CraftWorldInfo.this.vanillaChunkGenerator.getBiomeSource().possibleBiomes().stream()
            .map(biome -> org.bukkit.craftbukkit.v1_18_R2.block.CraftBlock.biomeBaseToBiome(CraftWorldInfo.this.biomeRegistry, biome))
            .toList();
        return new org.bukkit.generator.BiomeProvider() {
            @Override
            public org.bukkit.block.Biome getBiome(final WorldInfo worldInfo, final int x, final int y, final int z) {
                return org.bukkit.craftbukkit.v1_18_R2.block.CraftBlock.biomeBaseToBiome(CraftWorldInfo.this.biomeRegistry, CraftWorldInfo.this.vanillaChunkGenerator.getNoiseBiome(x >> 2, y >> 2, z >> 2));
            }

            @Override
            public java.util.List<org.bukkit.block.Biome> getBiomes(final org.bukkit.generator.WorldInfo worldInfo) {
                return possibleBiomes;
            }
        };
    }
    // Paper end
}
