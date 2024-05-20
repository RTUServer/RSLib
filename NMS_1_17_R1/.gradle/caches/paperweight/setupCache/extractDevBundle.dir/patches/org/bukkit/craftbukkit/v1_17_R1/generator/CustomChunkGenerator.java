package org.bukkit.craftbukkit.v1_17_R1.generator;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_17_R1.CraftHeightMap;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.generator.ChunkGenerator.ChunkData;

public class CustomChunkGenerator extends InternalChunkGenerator {

    public final net.minecraft.world.level.chunk.ChunkGenerator delegate;
    private final ChunkGenerator generator;
    private final ServerLevel world;
    private final Random random = new Random();
    private boolean newApi;
    private boolean implementBaseHeight = true;

    @Deprecated
    private class CustomBiomeGrid implements BiomeGrid {

        private final ChunkBiomeContainer biome; // SPIGOT-5529: stored in 4x4 grid

        public CustomBiomeGrid(ChunkBiomeContainer biome) {
            this.biome = biome;
        }

        @Override
        public Biome getBiome(int x, int z) {
            return this.getBiome(x, 0, z);
        }

        @Override
        public void setBiome(int x, int z, Biome bio) {
            for (int y = 0; y < CustomChunkGenerator.this.world.getWorld().getMaxHeight(); y += 4) {
                this.setBiome(x, y, z, bio);
            }
        }

        @Override
        public Biome getBiome(int x, int y, int z) {
            return CraftBlock.biomeBaseToBiome((Registry<net.minecraft.world.level.biome.Biome>) biome.biomeRegistry, this.biome.getNoiseBiome(x >> 2, y >> 2, z >> 2));
        }

        @Override
        public void setBiome(int x, int y, int z, Biome bio) {
            Preconditions.checkArgument(bio != Biome.CUSTOM, "Cannot set the biome to %s", bio);
            this.biome.setBiome(x >> 2, y >> 2, z >> 2, CraftBlock.biomeToBiomeBase((Registry<net.minecraft.world.level.biome.Biome>) biome.biomeRegistry, bio));
        }
    }

    public CustomChunkGenerator(ServerLevel world, net.minecraft.world.level.chunk.ChunkGenerator delegate, ChunkGenerator generator) {
        super(delegate.getBiomeSource(), delegate.getSettings());

        this.world = world;
        this.delegate = delegate;
        this.generator = generator;
    }

    @Override
    public net.minecraft.world.level.chunk.ChunkGenerator withSeed(long seed) {
        return new CustomChunkGenerator(this.world, this.delegate.withSeed(seed), this.generator);
    }

    @Override
    public BiomeSource getBiomeSource() {
        return this.delegate.getBiomeSource();
    }

    @Override
    public int getSeaLevel() {
        return this.delegate.getSeaLevel();
    }

    @Override
    public void createStructures(RegistryAccess registryManager, StructureFeatureManager accessor, ChunkAccess chunk, StructureManager structureManager, long worldSeed) {
        if (this.generator.shouldGenerateStructures()) {
            super.createStructures(registryManager, accessor, chunk, structureManager, worldSeed);
        }
    }

    @Override
    public void buildSurfaceAndBedrock(WorldGenRegion region, ChunkAccess chunk) {
        if (this.generator.shouldGenerateSurface()) {
            this.delegate.buildSurface(region, chunk);
        }

        CraftChunkData chunkData = new CraftChunkData(this.world.getWorld(), chunk);
        WorldgenRandom random = new WorldgenRandom();
        int x = chunk.getPos().x;
        int z = chunk.getPos().z;
        random.setBaseChunkSeed(x, z); // PAIL rename surfaceSeeded
        this.generator.generateSurface(this.world.getWorld(), random, x, z, chunkData);

        if (this.generator.shouldGenerateBedrock()) {
            random = new WorldgenRandom();
            random.setBaseChunkSeed(x, z); // PAIL rename surfaceSeeded
            this.delegate.buildBedrock(chunk, random);
        }

        random = new WorldgenRandom();
        random.setBaseChunkSeed(x, z); // PAIL rename surfaceSeeded
        this.generator.generateBedrock(this.world.getWorld(), random, x, z, chunkData);
        chunkData.breakLink();

        // return if new api is used
        if (this.newApi) {
            return;
        }

        // old ChunkGenerator logic, for backwards compatibility
        // Call the bukkit ChunkGenerator before structure generation so correct biome information is available.
        this.random.setSeed((long) x * 341873128712L + (long) z * 132897987541L);

        // Get default biome data for chunk
        CustomBiomeGrid biomegrid = new CustomBiomeGrid(new ChunkBiomeContainer(this.world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), region, chunk.getPos(), this.getBiomeSource()));

        ChunkData data;
        try {
            if (this.generator.isParallelCapable()) {
                data = this.generator.generateChunkData(this.world.getWorld(), this.random, x, z, biomegrid);
            } else {
                synchronized (this) {
                    data = this.generator.generateChunkData(this.world.getWorld(), this.random, x, z, biomegrid);
                }
            }
        } catch (UnsupportedOperationException exception) {
            this.newApi = true;
            return;
        }

        Preconditions.checkArgument(data instanceof OldCraftChunkData, "Plugins must use createChunkData(World) rather than implementing ChunkData: %s", data);
        OldCraftChunkData craftData = (OldCraftChunkData) data;
        LevelChunkSection[] sections = craftData.getRawChunkData();

        LevelChunkSection[] csect = chunk.getSections();
        int scnt = Math.min(csect.length, sections.length);

        // Loop through returned sections
        for (int sec = 0; sec < scnt; sec++) {
            if (sections[sec] == null) {
                continue;
            }
            LevelChunkSection section = sections[sec];

            csect[sec] = section;
        }

        // Set biome grid
        ((ProtoChunk) chunk).setBiomes(biomegrid.biome);

        if (craftData.getTiles() != null) {
            for (BlockPos pos : craftData.getTiles()) {
                int tx = pos.getX();
                int ty = pos.getY();
                int tz = pos.getZ();
                BlockState block = craftData.getTypeId(tx, ty, tz);

                if (block.hasBlockEntity()) {
                    BlockEntity tile = ((EntityBlock) block.getBlock()).newBlockEntity(new BlockPos((x << 4) + tx, ty, (z << 4) + tz), block);
                    chunk.setBlockEntity(tile);
                }
            }
        }

        // Apply captured light blocks
        for (BlockPos lightPosition : craftData.getLights()) {
            ((ProtoChunk) chunk).addLight(new BlockPos((x << 4) + lightPosition.getX(), lightPosition.getY(), (z << 4) + lightPosition.getZ())); // PAIL rename addLightBlock
        }

        // Paper start
        io.papermc.paper.world.generation.CraftProtoWorld protoWorld = new io.papermc.paper.world.generation.CraftProtoWorld(region);
        generator.generateDecorations(protoWorld);
        protoWorld.clearReference(); // make sure people dont try to use the ProtoWorld after we're done with it.
        // Paper end
    }

    @Override
    public void applyCarvers(long seed, BiomeManager access, ChunkAccess chunk, GenerationStep.Carving carver) {
        if (this.generator.shouldGenerateCaves()) {
            super.applyCarvers(seed, access, chunk, carver);
        }

        if (carver == GenerationStep.Carving.LIQUID) { // stage check ensures that the method is only called once
            CraftChunkData chunkData = new CraftChunkData(this.world.getWorld(), chunk);
            WorldgenRandom random = new WorldgenRandom();
            int x = chunk.getPos().x;
            int z = chunk.getPos().z;
            random.setLargeFeatureSeed(seed, 0, 0); // PAIL rename carvingSeeded

            this.generator.generateCaves(this.world.getWorld(), random, x, z, chunkData);
            chunkData.breakLink();
        }
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, StructureFeatureManager accessor, ChunkAccess chunk) {
        CompletableFuture<ChunkAccess> future = null;
        if (this.generator.shouldGenerateNoise()) {
            future = this.delegate.fillFromNoise(executor, accessor, chunk);
        }

        java.util.function.Function<ChunkAccess, ChunkAccess> function = (ichunkaccess1) -> {
            CraftChunkData chunkData = new CraftChunkData(this.world.getWorld(), ichunkaccess1);
            WorldgenRandom random = new WorldgenRandom();
            int x = ichunkaccess1.getPos().x;
            int z = ichunkaccess1.getPos().z;
            random.setSeed((long) x * 341873128712L + (long) z * 132897987541L);

            this.generator.generateNoise(this.world.getWorld(), random, x, z, chunkData);
            chunkData.breakLink();
            return ichunkaccess1;
        };

        return future == null ? CompletableFuture.supplyAsync(() -> function.apply(chunk), net.minecraft.Util.backgroundExecutor()) : future.thenApply(function);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmap, LevelHeightAccessor world) {
        if (this.implementBaseHeight) {
            try {
                WorldgenRandom random = new WorldgenRandom();
                int xChunk = x >> 4;
                int zChunk = z >> 4;
                random.setSeed((long) xChunk * 341873128712L + (long) zChunk * 132897987541L);

                return this.generator.getBaseHeight(this.world.getWorld(), random, x, z, CraftHeightMap.fromNMS(heightmap));
            } catch (UnsupportedOperationException exception) {
                this.implementBaseHeight = false;
            }
        }

        return this.delegate.getBaseHeight(x, z, heightmap, world);
    }

    @Override
    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(net.minecraft.world.level.biome.Biome biome, StructureFeatureManager accessor, MobCategory group, BlockPos pos) {
        return this.delegate.getMobsAt(biome, accessor, group, pos);
    }

    @Override
    public void applyBiomeDecoration(WorldGenRegion region, StructureFeatureManager accessor) {
        super.addDecorations(region, accessor, this.generator.shouldGenerateDecorations());
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        if (this.generator.shouldGenerateMobs()) {
            this.delegate.spawnOriginalMobs(region);
        }
    }

    @Override
    public int getSpawnHeight(LevelHeightAccessor world) {
        return this.delegate.getSpawnHeight(world);
    }

    @Override
    public int getGenDepth() {
        return this.delegate.getGenDepth();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor world) {
        return this.delegate.getBaseColumn(x, z, world);
    }

    @Override
    protected Codec<? extends net.minecraft.world.level.chunk.ChunkGenerator> codec() {
        throw new UnsupportedOperationException("Cannot serialize CustomChunkGenerator");
    }
}
