package com.destroystokyo.paper;

import java.util.Arrays;
import java.util.List;

import java.util.stream.Collectors;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.world.entity.MobCategory;
import com.destroystokyo.paper.antixray.ChunkPacketBlockControllerAntiXray.EngineMode;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.spigotmc.SpigotWorldConfig;

import static com.destroystokyo.paper.PaperConfig.log;
import static com.destroystokyo.paper.PaperConfig.logError;
import static com.destroystokyo.paper.PaperConfig.saveConfig;

public class PaperWorldConfig {

    private final String worldName;
    private final SpigotWorldConfig spigotConfig;
    private YamlConfiguration config;
    private boolean verbose;

    public PaperWorldConfig(String worldName, SpigotWorldConfig spigotConfig) {
        this.worldName = worldName;
        this.spigotConfig = spigotConfig;
        this.config = PaperConfig.config;
        init();
    }

    public void init() {
        this.config = PaperConfig.config; // grab updated reference
        log("-------- World Settings For [" + worldName + "] --------");
        PaperConfig.readConfig(PaperWorldConfig.class, this);
    }

    private void set(String path, Object val) {
        config.set("world-settings.default." + path, val);
        if (config.get("world-settings." + worldName + "." + path) != null) {
            config.set("world-settings." + worldName + "." + path, val);
        }
    }

    private void remove(String path) {
        config.addDefault("world-settings.default." + path, null);
        set(path, null);
    }

    public void removeOldValues() {
        boolean needsSave = false;

        if (PaperConfig.version < 24) {
            needsSave = true;

            set("despawn-ranges.soft", null);
            set("despawn-ranges.hard", null);

            set("spawn-limits.monsters", null);
            set("spawn-limits.animals", null);
            set("spawn-limits.water-animals", null);
            set("spawn-limits.water-ambient", null);
        }

        if (this.config.isSet("world-settings.default.treasure-maps-return-already-discovered") || this.config.isSet("world-settings." + worldName + ".treasure-maps-return-already-discovered")) {
            set("treasure-maps-return-already-discovered", null);
            needsSave = true;
        }

        if (needsSave) {
            saveConfig();
        }
    }

    public boolean zombiesTargetTurtleEggs = true;
    private void zombiesTargetTurtleEggs() {
        zombiesTargetTurtleEggs = getBoolean("zombies-target-turtle-eggs", zombiesTargetTurtleEggs);
    }

    public boolean piglinsGuardChests = true;
    private void piglinsGuardChests() {
        piglinsGuardChests = getBoolean("piglins-guard-chests", piglinsGuardChests);
    }

    public enum RedstoneImplementation {
        VANILLA, EIGENCRAFT, ALTERNATE_CURRENT
    }
    public RedstoneImplementation redstoneImplementation = RedstoneImplementation.VANILLA;
    private void redstoneImplementation() {
        String implementation;
        if (PaperConfig.version < 27) {
            implementation = "vanilla";
            if (config.contains("world-settings.default.use-faster-eigencraft-redstone")) {
                implementation = config.getBoolean("world-settings.default.use-faster-eigencraft-redstone") ? "eigencraft" : "vanilla";
                config.set("world-settings.default.redstone-implementation", implementation);
            }
            if (config.contains("world-settings." + worldName + ".use-faster-eigencraft-redstone")) {
                implementation = config.getBoolean("world-settings." + worldName + ".use-faster-eigencraft-redstone") ? "eigencraft" : "vanilla";
                config.set("world-settings." + worldName + ".redstone-implementation", implementation);
            }
            remove("use-faster-eigencraft-redstone");
        } else {
            implementation = this.getString("redstone-implementation", "vanilla").toLowerCase().trim();
        }
        switch (implementation) {
            default:
                logError("Invalid redstone-implementation config " + implementation + " - must be one of: vanilla, eigencraft, alternate-current");
            case "vanilla":
                redstoneImplementation = RedstoneImplementation.VANILLA;
                log("Using the Vanilla redstone implementation.");
                break;
            case "eigencraft":
                redstoneImplementation = RedstoneImplementation.EIGENCRAFT;
                log("Using Eigencraft's redstone implementation by theosib.");
                break;
            case "alternate-current":
                redstoneImplementation = RedstoneImplementation.ALTERNATE_CURRENT;
                log("Using Alternate Current's redstone implementation by Space Walker.");
                break;
        }
    }

    public boolean shouldRemoveDragon = false;
    private void shouldRemoveDragon() {
        shouldRemoveDragon = getBoolean("should-remove-dragon", shouldRemoveDragon);
        if (shouldRemoveDragon) {
            log("The Ender Dragon will be removed if she already exists without a portal.");
        }
    }

    public boolean onlyPlayersCollide = false;
    public boolean allowVehicleCollisions = true;
    private void onlyPlayersCollide() {
        onlyPlayersCollide = getBoolean("only-players-collide", onlyPlayersCollide);
        allowVehicleCollisions = getBoolean("allow-vehicle-collisions", allowVehicleCollisions);
        if (onlyPlayersCollide && !allowVehicleCollisions) {
            log("Collisions will only work if a player is one of the two entities colliding.");
        } else if (onlyPlayersCollide) {
            log("Collisions will only work if a player OR a vehicle is one of the two entities colliding.");
        }
    }

    public int wanderingTraderSpawnMinuteTicks = 1200;
    public int wanderingTraderSpawnDayTicks = 24000;
    public int wanderingTraderSpawnChanceFailureIncrement = 25;
    public int wanderingTraderSpawnChanceMin = 25;
    public int wanderingTraderSpawnChanceMax = 75;
    private void wanderingTraderSettings() {
        wanderingTraderSpawnMinuteTicks = getInt("wandering-trader.spawn-minute-length", wanderingTraderSpawnMinuteTicks);
        wanderingTraderSpawnDayTicks = getInt("wandering-trader.spawn-day-length", wanderingTraderSpawnDayTicks);
        wanderingTraderSpawnChanceFailureIncrement = getInt("wandering-trader.spawn-chance-failure-increment", wanderingTraderSpawnChanceFailureIncrement);
        wanderingTraderSpawnChanceMin = getInt("wandering-trader.spawn-chance-min", wanderingTraderSpawnChanceMin);
        wanderingTraderSpawnChanceMax = getInt("wandering-trader.spawn-chance-max", wanderingTraderSpawnChanceMax);
    }

    public boolean fixClimbingBypassingCrammingRule = false;
    private void fixClimbingBypassingCrammingRule() {
        fixClimbingBypassingCrammingRule = getBoolean("fix-climbing-bypassing-cramming-rule", fixClimbingBypassingCrammingRule);
    }

    public boolean fixCuringZombieVillagerDiscountExploit = true;
    private void fixCuringExploit() {
        fixCuringZombieVillagerDiscountExploit = getBoolean("game-mechanics.fix-curing-zombie-villager-discount-exploit", fixCuringZombieVillagerDiscountExploit);
    }

    public boolean disableMobSpawnerSpawnEggTransformation = false;
    private void disableMobSpawnerSpawnEggTransformation() {
        disableMobSpawnerSpawnEggTransformation = getBoolean("game-mechanics.disable-mob-spawner-spawn-egg-transformation", disableMobSpawnerSpawnEggTransformation);
    }
    
    private final List<net.minecraft.world.entity.EntityType<?>> entitiesValidForBreakDoors = Arrays.asList(net.minecraft.world.entity.EntityType.ZOMBIE, net.minecraft.world.entity.EntityType.ZOMBIE_VILLAGER, net.minecraft.world.entity.EntityType.HUSK, net.minecraft.world.entity.EntityType.ZOMBIFIED_PIGLIN, net.minecraft.world.entity.EntityType.VINDICATOR);
    public java.util.Map<net.minecraft.world.entity.EntityType<?>, java.util.List<net.minecraft.world.Difficulty>> entitiesDifficultyBreakDoors = new it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap<>();
    private void setupEntityBreakingDoors() {
        for (net.minecraft.world.entity.EntityType<?> entityType : entitiesValidForBreakDoors) {
            java.util.function.Predicate<net.minecraft.world.Difficulty> difficultyPredicate = net.minecraft.world.entity.monster.Zombie.DOOR_BREAKING_PREDICATE;
            if (entityType == net.minecraft.world.entity.EntityType.VINDICATOR) {
                difficultyPredicate = net.minecraft.world.entity.monster.Vindicator.DOOR_BREAKING_PREDICATE;
            }
            entitiesDifficultyBreakDoors.put(
                entityType,
                getEnumList(
                    "door-breaking-difficulty." + entityType.id,
                    java.util.Arrays.stream(net.minecraft.world.Difficulty.values())
                        .filter(difficultyPredicate)
                        .collect(Collectors.toList()),
                    net.minecraft.world.Difficulty.class
                )
            );
        }
    }

    public Map<net.minecraft.world.entity.EntityType<?>, Integer> entityPerChunkSaveLimits = new HashMap<>();
    private void entityPerChunkSaveLimits() {
        getInt("entity-per-chunk-save-limit.experience_orb", -1);
        getInt("entity-per-chunk-save-limit.snowball", -1);
        getInt("entity-per-chunk-save-limit.ender_pearl", -1);
        getInt("entity-per-chunk-save-limit.arrow", -1);
        getInt("entity-per-chunk-save-limit.fireball", -1);
        getInt("entity-per-chunk-save-limit.small_fireball", -1);

        addEntityPerChunkSaveLimitsFromSection(config.getConfigurationSection("world-settings.default.entity-per-chunk-save-limit"), entityPerChunkSaveLimits);
        addEntityPerChunkSaveLimitsFromSection(config.getConfigurationSection("world-settings." + worldName + ".entity-per-chunk-save-limit"), entityPerChunkSaveLimits);
    }

    private static void addEntityPerChunkSaveLimitsFromSection(final org.bukkit.configuration.ConfigurationSection section, final Map<net.minecraft.world.entity.EntityType<?>, Integer> limitMap) {
        if (section == null) {
            return;
        }
        for (final String key : section.getKeys(false)) {
            final int value = section.getInt(key);
            final net.minecraft.world.entity.EntityType<?> type = net.minecraft.world.entity.EntityType.byString(key).orElse(null);
            if (type == null) {
                logError("Invalid entity-per-chunk-save-limit config, '" + key+ "' is not a valid entity type. Correct this in paper.yml.");
                continue;
            }
            if (value >= 0) {
                limitMap.put(type, value);
            } else {
                limitMap.remove(type);
            }
        }
    }

    public short keepLoadedRange;
    private void keepLoadedRange() {
        keepLoadedRange = (short) (getInt("keep-spawn-loaded-range", Math.min(spigotConfig.viewDistance, 10)) * 16);
        log( "Keep Spawn Loaded Range: " + (keepLoadedRange/16));
    }

    public int autoSavePeriod = -1;
    private void autoSavePeriod() {
        autoSavePeriod = getInt("auto-save-interval", -1);
        if (autoSavePeriod > 0) {
            log("Auto Save Interval: " +autoSavePeriod + " (" + (autoSavePeriod / 20) + "s)");
        } else if (autoSavePeriod < 0) {
            autoSavePeriod = net.minecraft.server.MinecraftServer.getServer().autosavePeriod;
        }
    }

    public int maxAutoSaveChunksPerTick = 24;
    private void maxAutoSaveChunksPerTick() {
        maxAutoSaveChunksPerTick = getInt("max-auto-save-chunks-per-tick", 24);
    }

    private boolean getBoolean(String path, boolean def) {
        return this.getBoolean(path, def, true);
    }
    private boolean getBoolean(String path, boolean def, boolean setDefault) {
        if (setDefault) {
            config.addDefault("world-settings.default." + path, def);
        }
        return config.getBoolean("world-settings." + worldName + "." + path, config.getBoolean("world-settings.default." + path, def));
    }

    private double getDouble(String path, double def) {
        config.addDefault("world-settings.default." + path, def);
        return config.getDouble("world-settings." + worldName + "." + path, config.getDouble("world-settings.default." + path));
    }

    private int getInt(String path, int def) {
        return getInt(path, def, true);
    }

    private int getInt(String path, int def, boolean setDefault) {
        if (setDefault) {
            config.addDefault("world-settings.default." + path, def);
        }
        return config.getInt("world-settings." + worldName + "." + path, config.getInt("world-settings.default." + path, def));
    }

    private long getLong(String path, long def) {
        config.addDefault("world-settings.default." + path, def);
        return config.getLong("world-settings." + worldName + "." + path, config.getLong("world-settings.default." + path));
    }

    private float getFloat(String path, float def) {
        // TODO: Figure out why getFloat() always returns the default value.
        return (float) getDouble(path, (double) def);
    }

    private <T> List<T> getList(String path, List<T> def) {
        config.addDefault("world-settings.default." + path, def);
        return (List<T>) config.getList("world-settings." + worldName + "." + path, config.getList("world-settings.default." + path));
    }

    private String getString(String path, String def) {
        config.addDefault("world-settings.default." + path, def);
        return config.getString("world-settings." + worldName + "." + path, config.getString("world-settings.default." + path));
    }

    private <T extends Enum<T>> List<T> getEnumList(String path, List<T> def, Class<T> type) {
        config.addDefault("world-settings.default." + path, def.stream().map(Enum::name).collect(Collectors.toList()));
        return ((List<String>) (config.getList("world-settings." + worldName + "." + path, config.getList("world-settings.default." + path)))).stream().map(s -> Enum.valueOf(type, s)).collect(Collectors.toList());
    }

    public int cactusMaxHeight;
    public int reedMaxHeight;
    public int bambooMaxHeight;
    public int bambooMinHeight;
    private void blockGrowthHeight() {
        cactusMaxHeight = getInt("max-growth-height.cactus", 3);
        reedMaxHeight = getInt("max-growth-height.reeds", 3);
        bambooMaxHeight = getInt("max-growth-height.bamboo.max", 16);
        bambooMinHeight = getInt("max-growth-height.bamboo.min", 11);
        log("Max height for cactus growth " + cactusMaxHeight + ". Max height for reed growth " + reedMaxHeight + ". Max height for bamboo growth " + bambooMaxHeight + ". Min height for fully-grown bamboo " + bambooMinHeight + ".");
    }

    public double babyZombieMovementModifier;
    private void babyZombieMovementModifier() {
        babyZombieMovementModifier = getDouble("baby-zombie-movement-modifier", 0.5D);
        if (PaperConfig.version < 20) {
            babyZombieMovementModifier = getDouble("baby-zombie-movement-speed", 0.5D);
            set("baby-zombie-movement-modifier", babyZombieMovementModifier);
        }

        log("Baby zombies will move at the speed of " + babyZombieMovementModifier);
    }

    public int fishingMinTicks;
    public int fishingMaxTicks;
    private void fishingTickRange() {
        fishingMinTicks = getInt("fishing-time-range.MinimumTicks", 100);
        fishingMaxTicks = getInt("fishing-time-range.MaximumTicks", 600);
        log("Fishing time ranges are between " + fishingMinTicks +" and " + fishingMaxTicks + " ticks");
    }

    public boolean nerfedMobsShouldJump;
    private void nerfedMobsShouldJump() {
        nerfedMobsShouldJump = getBoolean("spawner-nerfed-mobs-should-jump", false);
    }

    public final Reference2IntMap<MobCategory> softDespawnDistances = new Reference2IntOpenHashMap<>(MobCategory.values().length);
    public final Reference2IntMap<MobCategory> hardDespawnDistances = new Reference2IntOpenHashMap<>(MobCategory.values().length);
    private void despawnDistances() {
        if (PaperConfig.version < 24) {
            int softDistance = getInt("despawn-ranges.soft", 32, false); // 32^2 = 1024, Minecraft Default
            int hardDistance = getInt("despawn-ranges.hard", 128, false); // 128^2 = 16384, Minecraft Default
            for (MobCategory value : MobCategory.values()) {
                if (softDistance != 32) {
                    softDespawnDistances.put(value, softDistance);
                }
                if (hardDistance != 128) {
                    hardDespawnDistances.put(value, hardDistance);
                }
            }
        }
        for (MobCategory category : MobCategory.values()) {
            int softDistance = getInt("despawn-ranges." + category.getName() + ".soft", softDespawnDistances.getOrDefault(category, category.getNoDespawnDistance()));
            int hardDistance = getInt("despawn-ranges." + category.getName() + ".hard", hardDespawnDistances.getOrDefault(category, category.getDespawnDistance()));
            if (softDistance > hardDistance) {
                softDistance = hardDistance;
            }
            log("Mobs in " + category.getName() + " Despawn Ranges: Soft" + softDistance + " Hard: " + hardDistance);
            softDespawnDistances.put(category, softDistance);
            hardDespawnDistances.put(category, hardDistance);
        }
    }

    public boolean keepSpawnInMemory;
    private void keepSpawnInMemory() {
        keepSpawnInMemory = getBoolean("keep-spawn-loaded", true);
        log("Keep spawn chunk loaded: " + keepSpawnInMemory);
    }

    public int fallingBlockHeightNerf;
    public int entityTNTHeightNerf;
    private void heightNerfs() {
        fallingBlockHeightNerf = getInt("falling-block-height-nerf", 0);
        entityTNTHeightNerf = getInt("tnt-entity-height-nerf", 0);

        if (fallingBlockHeightNerf != 0) log("Falling Block Height Limit set to Y: " + fallingBlockHeightNerf);
        if (entityTNTHeightNerf != 0) log("TNT Entity Height Limit set to Y: " + entityTNTHeightNerf);
    }

    public int netherVoidTopDamageHeight;
    public boolean doNetherTopVoidDamage() { return netherVoidTopDamageHeight > 0; }
    private void netherVoidTopDamageHeight() {
        netherVoidTopDamageHeight = getInt("nether-ceiling-void-damage-height", 0);
        log("Top of the nether void damage height: " + netherVoidTopDamageHeight);

        if (PaperConfig.version < 18) {
            boolean legacy = getBoolean("nether-ceiling-void-damage", false);
            if (legacy) {
                netherVoidTopDamageHeight = 128;
                set("nether-ceiling-void-damage-height", netherVoidTopDamageHeight);
            }
        }
    }

    public float maxLeashDistance = 10f;
    private void maxLeashDistance() {
        maxLeashDistance = getFloat("max-leash-distance", maxLeashDistance);
        log("Max leash distance: " + maxLeashDistance);
    }

    public boolean disableEndCredits;
    private void disableEndCredits() {
        disableEndCredits = getBoolean("game-mechanics.disable-end-credits", false);
        log("End credits disabled: " + disableEndCredits);
    }

    public boolean optimizeExplosions;
    private void optimizeExplosions() {
        optimizeExplosions = getBoolean("optimize-explosions", false);
        log("Optimize explosions: " + optimizeExplosions);
    }

    public boolean disableExplosionKnockback;
    private void disableExplosionKnockback(){
        disableExplosionKnockback = getBoolean("disable-explosion-knockback", false);
    }

    public boolean disableThunder;
    private void disableThunder() {
        disableThunder = getBoolean("disable-thunder", false);
    }

    public boolean disableIceAndSnow;
    private void disableIceAndSnow(){
        disableIceAndSnow = getBoolean("disable-ice-and-snow", false);
    }

    public int mobSpawnerTickRate;
    private void mobSpawnerTickRate() {
        mobSpawnerTickRate = getInt("mob-spawner-tick-rate", 1);
    }

    public Integer waterAnimalMaxSpawnHeight;
    private void waterAnimalMaxSpawnHeight() {
        String v = getString("wateranimal-spawn-height.maximum", "default");
        try {
            waterAnimalMaxSpawnHeight = Integer.parseInt(v);
        } catch (NumberFormatException ignored) {
        }
    }

    public Integer waterAnimalMinSpawnHeight;
    private void waterAnimalMinSpawnHeight() {
        String v = getString("wateranimal-spawn-height.minimum", "default");
        try {
            waterAnimalMinSpawnHeight = Integer.parseInt(v);
        } catch (NumberFormatException ignored) {
        }
    }

    public int containerUpdateTickRate;
    private void containerUpdateTickRate() {
        containerUpdateTickRate = getInt("container-update-tick-rate", 1);
    }

    public boolean disableChestCatDetection;
    private void disableChestCatDetection() {
        disableChestCatDetection = getBoolean("game-mechanics.disable-chest-cat-detection", false);
    }

    public boolean disablePlayerCrits;
    private void disablePlayerCrits() {
        disablePlayerCrits = getBoolean("game-mechanics.disable-player-crits", false);
    }

    public boolean allChunksAreSlimeChunks;
    private void allChunksAreSlimeChunks() {
        allChunksAreSlimeChunks = getBoolean("all-chunks-are-slime-chunks", false);
    }

    public double slimeMaxSpawnHeightInSwamp = 70;
    public double slimeMinSpawnHeightInSwamp = 50;
    public double slimeMaxSpawnHeightInSlimeChunks = 40;
    private void slimeSpawnHeight() {
        slimeMaxSpawnHeightInSwamp = getDouble("slime-spawn-height.swamp-biome.maximum", this.slimeMaxSpawnHeightInSwamp);
        slimeMinSpawnHeightInSwamp = getDouble("slime-spawn-height.swamp-biome.minimum", this.slimeMinSpawnHeightInSwamp);
        slimeMaxSpawnHeightInSlimeChunks = getDouble("slime-spawn-height.slime-chunk.maximum", this.slimeMaxSpawnHeightInSlimeChunks);
    }


    public int portalSearchRadius;
    public int portalCreateRadius;
    public boolean portalSearchVanillaDimensionScaling;
    private void portalSearchRadius() {
        portalSearchRadius = getInt("portal-search-radius", 128);
        portalCreateRadius = getInt("portal-create-radius", 16);
        portalSearchVanillaDimensionScaling = getBoolean("portal-search-vanilla-dimension-scaling", true);
    }

    public boolean disableTeleportationSuffocationCheck;
    private void disableTeleportationSuffocationCheck() {
        disableTeleportationSuffocationCheck = getBoolean("disable-teleportation-suffocation-check", false);
    }

    public boolean nonPlayerEntitiesOnScoreboards = false;
    private void nonPlayerEntitiesOnScoreboards() {
        nonPlayerEntitiesOnScoreboards = getBoolean("allow-non-player-entities-on-scoreboards", false);
    }

    public int nonPlayerArrowDespawnRate = -1;
    public int creativeArrowDespawnRate = -1;
    private void nonPlayerArrowDespawnRate() {
        nonPlayerArrowDespawnRate = getInt("non-player-arrow-despawn-rate", -1);
        if (nonPlayerArrowDespawnRate == -1) {
            nonPlayerArrowDespawnRate = spigotConfig.arrowDespawnRate;
        }
        creativeArrowDespawnRate = getInt("creative-arrow-despawn-rate", -1);
        if (creativeArrowDespawnRate == -1) {
            creativeArrowDespawnRate = spigotConfig.arrowDespawnRate;
        }
        log("Non Player Arrow Despawn Rate: " + nonPlayerArrowDespawnRate);
        log("Creative Arrow Despawn Rate: " + creativeArrowDespawnRate);
    }

    public double skeleHorseSpawnChance;
    private void skeleHorseSpawnChance() {
        skeleHorseSpawnChance = getDouble("skeleton-horse-thunder-spawn-chance", 0.01D);
        if (skeleHorseSpawnChance < 0) {
            skeleHorseSpawnChance = 0.01D; // Vanilla value
        }
    }

    public int fixedInhabitedTime;
    private void fixedInhabitedTime() {
        if (PaperConfig.version < 16) {
            if (!config.getBoolean("world-settings.default.use-chunk-inhabited-timer", true)) config.set("world-settings.default.fixed-chunk-inhabited-time", 0);
            if (!config.getBoolean("world-settings." + worldName + ".use-chunk-inhabited-timer", true)) config.set("world-settings." + worldName + ".fixed-chunk-inhabited-time", 0);
            set("use-chunk-inhabited-timer", null);
        }
        fixedInhabitedTime = getInt("fixed-chunk-inhabited-time", -1);
    }

    public int grassUpdateRate = 1;
    private void grassUpdateRate() {
        grassUpdateRate = Math.max(0, getInt("grass-spread-tick-rate", grassUpdateRate));
        log("Grass Spread Tick Rate: " + grassUpdateRate);
    }

    public boolean useVanillaScoreboardColoring;
    private void useVanillaScoreboardColoring() {
        useVanillaScoreboardColoring = getBoolean("use-vanilla-world-scoreboard-name-coloring", false);
    }

    public boolean frostedIceEnabled = true;
    public int frostedIceDelayMin = 20;
    public int frostedIceDelayMax = 40;
    private void frostedIce() {
        this.frostedIceEnabled = this.getBoolean("frosted-ice.enabled", this.frostedIceEnabled);
        this.frostedIceDelayMin = this.getInt("frosted-ice.delay.min", this.frostedIceDelayMin);
        this.frostedIceDelayMax = this.getInt("frosted-ice.delay.max", this.frostedIceDelayMax);
        log("Frosted Ice: " + (this.frostedIceEnabled ? "enabled" : "disabled") + " / delay: min=" + this.frostedIceDelayMin + ", max=" + this.frostedIceDelayMax);
    }

    public boolean autoReplenishLootables;
    public boolean restrictPlayerReloot;
    public boolean changeLootTableSeedOnFill;
    public int maxLootableRefills;
    public int lootableRegenMin;
    public int lootableRegenMax;
    private void enhancedLootables() {
        autoReplenishLootables = getBoolean("lootables.auto-replenish", false);
        restrictPlayerReloot = getBoolean("lootables.restrict-player-reloot", true);
        changeLootTableSeedOnFill = getBoolean("lootables.reset-seed-on-fill", true);
        maxLootableRefills = getInt("lootables.max-refills", -1);
        lootableRegenMin = PaperConfig.getSeconds(getString("lootables.refresh-min", "12h"));
        lootableRegenMax = PaperConfig.getSeconds(getString("lootables.refresh-max", "2d"));
        if (autoReplenishLootables) {
            log("Lootables: Replenishing every " +
                PaperConfig.timeSummary(lootableRegenMin) + " to " +
                PaperConfig.timeSummary(lootableRegenMax) +
                (restrictPlayerReloot ? " (restricting reloot)" : "")
            );
        }
    }

    public boolean preventTntFromMovingInWater;
    private void preventTntFromMovingInWater() {
        if (PaperConfig.version < 13) {
            boolean oldVal = getBoolean("enable-old-tnt-cannon-behaviors", false);
            set("prevent-tnt-from-moving-in-water", oldVal);
        }
        preventTntFromMovingInWater = getBoolean("prevent-tnt-from-moving-in-water", false);
        log("Prevent TNT from moving in water: " + preventTntFromMovingInWater);
    }

    public boolean filterNBTFromSpawnEgg = true;
    private void fitlerNBTFromSpawnEgg() {
        filterNBTFromSpawnEgg = getBoolean("filter-nbt-data-from-spawn-eggs-and-related", true);
        if (!filterNBTFromSpawnEgg) {
            Bukkit.getLogger().warning("Spawn Egg and Armor Stand NBT filtering disabled, this is a potential security risk");
        }
    }

    public boolean enableTreasureMaps = true;
    public boolean treasureMapsAlreadyDiscoveredVillager = false;
    public Boolean treasureMapsAlreadyDiscoveredLootTable = null;
    private Boolean getBooleanOrNull(String path, Boolean defaultValue) {
        this.config.addDefault("world-settings.default." + path, defaultValue == null ? "default" : defaultValue);
        final Object value = this.config.get("world-settings." + worldName + "." + path, this.config.get("world-settings.default." + path));
        if (value instanceof Boolean bool) {
            return bool;
        }
        return null;
    }
    private void treasureMapsAlreadyDiscovered() {
        enableTreasureMaps = getBoolean("enable-treasure-maps", true);
        if (getBoolean("treasure-maps-return-already-discovered", false, false)) {
            treasureMapsAlreadyDiscoveredLootTable = true;
            treasureMapsAlreadyDiscoveredVillager = true;
        }
        treasureMapsAlreadyDiscoveredVillager = getBoolean("treasure-maps-find-already-discovered.villager-trade", treasureMapsAlreadyDiscoveredVillager);
        treasureMapsAlreadyDiscoveredLootTable = getBooleanOrNull("treasure-maps-find-already-discovered.loot-tables", treasureMapsAlreadyDiscoveredLootTable);
    }

    public int maxCollisionsPerEntity = 8;
    private void maxEntityCollision() {
        maxCollisionsPerEntity = getInt( "max-entity-collisions", this.spigotConfig.getInt("max-entity-collisions", this.maxCollisionsPerEntity, false) );
        log( "Max Entity Collisions: " + maxCollisionsPerEntity );
    }

    public boolean parrotsHangOnBetter;
    private void parrotsHangOnBetter() {
        parrotsHangOnBetter = getBoolean("parrots-are-unaffected-by-player-movement", false);
        log("Parrots are unaffected by player movement: " + parrotsHangOnBetter);
    }

    public boolean disableCreeperLingeringEffect;
    private void setDisableCreeperLingeringEffect() {
        disableCreeperLingeringEffect = getBoolean("disable-creeper-lingering-effect", false);
        log("Creeper lingering effect: " + disableCreeperLingeringEffect);
    }

    public boolean disableSprintInterruptionOnAttack;
    private void disableSprintInterruptionOnAttack() {
        disableSprintInterruptionOnAttack = getBoolean("game-mechanics.disable-sprint-interruption-on-attack", false);
    }

    public boolean disableEnderpearlExploit = true;
    private void disableEnderpearlExploit() {
        disableEnderpearlExploit = getBoolean("game-mechanics.disable-unloaded-chunk-enderpearl-exploit", disableEnderpearlExploit);
        log("Disable Unloaded Chunk Enderpearl Exploit: " + (disableEnderpearlExploit ? "enabled" : "disabled"));
    }

    public int shieldBlockingDelay = 5;
    private void shieldBlockingDelay() {
        shieldBlockingDelay = getInt("game-mechanics.shield-blocking-delay", 5);
    }

    public boolean scanForLegacyEnderDragon = true;
    private void scanForLegacyEnderDragon() {
        scanForLegacyEnderDragon = getBoolean("game-mechanics.scan-for-legacy-ender-dragon", true);
    }

    public boolean ironGolemsCanSpawnInAir = false;
    private void ironGolemsCanSpawnInAir() {
        ironGolemsCanSpawnInAir = getBoolean("iron-golems-can-spawn-in-air", ironGolemsCanSpawnInAir);
    }

    public boolean armorStandEntityLookups = true;
    private void armorStandEntityLookups() {
        armorStandEntityLookups = getBoolean("armor-stands-do-collision-entity-lookups", true);
    }

    public boolean armorStandTick = true;
    private void armorStandTick() {
        this.armorStandTick = this.getBoolean("armor-stands-tick", this.armorStandTick);
        log("ArmorStand ticking is " + (this.armorStandTick ? "enabled" : "disabled") + " by default");
    }

    public int waterOverLavaFlowSpeed;
    private void waterOverLavaFlowSpeed() {
        waterOverLavaFlowSpeed = getInt("water-over-lava-flow-speed", 5);
        log("Water over lava flow speed: " + waterOverLavaFlowSpeed);
    }

    public boolean preventMovingIntoUnloadedChunks = false;
    private void preventMovingIntoUnloadedChunks() {
        preventMovingIntoUnloadedChunks = getBoolean("prevent-moving-into-unloaded-chunks", false);
    }

    public enum DuplicateUUIDMode {
        SAFE_REGEN, DELETE, NOTHING, WARN
    }
    public DuplicateUUIDMode duplicateUUIDMode = DuplicateUUIDMode.SAFE_REGEN;
    public int duplicateUUIDDeleteRange = 32;
    private void repairDuplicateUUID() {
        String desiredMode = getString("duplicate-uuid-resolver", "saferegen").toLowerCase().trim();
        duplicateUUIDDeleteRange = getInt("duplicate-uuid-saferegen-delete-range", duplicateUUIDDeleteRange);
        switch (desiredMode.toLowerCase()) {
            case "regen":
            case "regenerate":
            case "saferegen":
            case "saferegenerate":
                duplicateUUIDMode = DuplicateUUIDMode.SAFE_REGEN;
                log("Duplicate UUID Resolve: Regenerate New UUID if distant (Delete likely duplicates within " + duplicateUUIDDeleteRange + " blocks)");
                break;
            case "remove":
            case "delete":
                duplicateUUIDMode = DuplicateUUIDMode.DELETE;
                log("Duplicate UUID Resolve: Delete Entity");
                break;
            case "silent":
            case "nothing":
                duplicateUUIDMode = DuplicateUUIDMode.NOTHING;
                logError("Duplicate UUID Resolve: Do Nothing (no logs) - Warning, may lose indication of bad things happening");
                break;
            case "log":
            case "warn":
                duplicateUUIDMode = DuplicateUUIDMode.WARN;
                log("Duplicate UUID Resolve: Warn (do nothing but log it happened, may be spammy)");
                break;
            default:
                duplicateUUIDMode = DuplicateUUIDMode.WARN;
                logError("Warning: Invalid duplicate-uuid-resolver config " + desiredMode + " - must be one of: regen, delete, nothing, warn");
                log("Duplicate UUID Resolve: Warn (do nothing but log it happened, may be spammy)");
                break;
        }
    }

    public boolean countAllMobsForSpawning = false;
    private void countAllMobsForSpawning() {
        countAllMobsForSpawning = getBoolean("count-all-mobs-for-spawning", false);
        if (countAllMobsForSpawning) {
            log("Counting all mobs for spawning. Mob farms may reduce natural spawns elsewhere in world.");
        } else {
            log("Using improved mob spawn limits (Only Natural Spawns impact spawn limits for more natural spawns)");
        }
    }

    public boolean disableRelativeProjectileVelocity;
    private void disableRelativeProjectileVelocity() {
        disableRelativeProjectileVelocity = getBoolean("game-mechanics.disable-relative-projectile-velocity", false);
    }

    public boolean disablePillagerPatrols = false;
    public double patrolSpawnChance = 0.2;
    public boolean patrolPerPlayerDelay = false;
    public int patrolDelay = 12000;
    public boolean patrolPerPlayerStart = false;
    public int patrolStartDay = 5;
    private void pillagerSettings() {
        disablePillagerPatrols = getBoolean("game-mechanics.disable-pillager-patrols", disablePillagerPatrols);
        patrolSpawnChance = getDouble("game-mechanics.pillager-patrols.spawn-chance", patrolSpawnChance);
        patrolPerPlayerDelay = getBoolean("game-mechanics.pillager-patrols.spawn-delay.per-player", patrolPerPlayerDelay);
        patrolDelay = getInt("game-mechanics.pillager-patrols.spawn-delay.ticks", patrolDelay);
        patrolPerPlayerStart = getBoolean("game-mechanics.pillager-patrols.start.per-player", patrolPerPlayerStart);
        patrolStartDay = getInt("game-mechanics.pillager-patrols.start.day", patrolStartDay);
    }

    public boolean generateFlatBedrock = false;
    private void generatorSettings() {
        generateFlatBedrock = getBoolean("generator-settings.flat-bedrock", this.generateFlatBedrock);
    }

    public boolean entitiesTargetWithFollowRange = false;
    private void entitiesTargetWithFollowRange() {
        entitiesTargetWithFollowRange = getBoolean("entities-target-with-follow-range", entitiesTargetWithFollowRange);
    }

    public boolean cooldownHopperWhenFull = true;
    public boolean disableHopperMoveEvents = false;
    public boolean hoppersIgnoreOccludingBlocks = false;
    private void hopperOptimizations() {
        cooldownHopperWhenFull = getBoolean("hopper.cooldown-when-full", cooldownHopperWhenFull);
        log("Cooldown Hoppers when Full: " + (cooldownHopperWhenFull ? "enabled" : "disabled"));
        disableHopperMoveEvents = getBoolean("hopper.disable-move-event", disableHopperMoveEvents);
        log("Hopper Move Item Events: " + (disableHopperMoveEvents ? "disabled" : "enabled"));
        hoppersIgnoreOccludingBlocks = getBoolean("hopper.ignore-occluding-blocks", hoppersIgnoreOccludingBlocks);
        log("Hopper Ignore Container Entities inside Occluding Blocks: " + (hoppersIgnoreOccludingBlocks ? "enabled" : "disabled"));
    }

    public boolean undeadImmuneToCertainEffects = true;
    public boolean spidersImmuneToPoisonEffect = true;
    public boolean witherImmuneToWitherEffect = true;
    public boolean witherSkeletonImmuneToWitherEffect = true;
    private void mobEffectChanges() {
        undeadImmuneToCertainEffects = getBoolean("mob-effects.undead-immune-to-certain-effects", undeadImmuneToCertainEffects);
        log("Undead immune to harmful effects: " + undeadImmuneToCertainEffects);
        spidersImmuneToPoisonEffect = getBoolean("mob-effects.spiders-immune-to-poison-effect", spidersImmuneToPoisonEffect);
        log("Spiders immune to poison effect: " + spidersImmuneToPoisonEffect);
        witherImmuneToWitherEffect = getBoolean("mob-effects.immune-to-wither-effect.wither", witherImmuneToWitherEffect);
        log("Wither immune to wither effect: " + witherImmuneToWitherEffect);
        witherSkeletonImmuneToWitherEffect = getBoolean("mob-effects.immune-to-wither-effect.wither-skeleton", witherSkeletonImmuneToWitherEffect);
        log("Wither skeleton immune to wither effect: " + witherSkeletonImmuneToWitherEffect);
    }

    public boolean nerfNetherPortalPigmen = false;
    private void nerfNetherPortalPigmen() {
        nerfNetherPortalPigmen = getBoolean("game-mechanics.nerf-pigmen-from-nether-portals", nerfNetherPortalPigmen);
    }

    public double zombieVillagerInfectionChance = -1.0;
    private void zombieVillagerInfectionChance() {
        zombieVillagerInfectionChance = getDouble("zombie-villager-infection-chance", zombieVillagerInfectionChance);
    }

    public Reference2IntMap<MobCategory> perWorldSpawnLimits = new Reference2IntOpenHashMap<>(net.minecraft.world.level.NaturalSpawner.SPAWNING_CATEGORIES.length);
    private void perWorldSpawnLimits() {
        perWorldSpawnLimits.defaultReturnValue(-1);
        if (PaperConfig.version < 24) {
            // ambient category already had correct name
            perWorldSpawnLimits.put(MobCategory.MONSTER, getInt("spawn-limits.monsters", -1, false));
            perWorldSpawnLimits.put(MobCategory.CREATURE, getInt("spawn-limits.animals", -1, false));
            perWorldSpawnLimits.put(MobCategory.WATER_CREATURE, getInt("spawn-limits.water-animals", -1, false));
            perWorldSpawnLimits.put(MobCategory.WATER_AMBIENT, getInt("spawn-limits.water-ambient", -1, false));
        }
        for (MobCategory value : net.minecraft.world.level.NaturalSpawner.SPAWNING_CATEGORIES) {
            perWorldSpawnLimits.put(value, getInt("spawn-limits." + value.getName(), perWorldSpawnLimits.getInt(value)));
        }
    }

    public int lightQueueSize = 20;
    private void lightQueueSize() {
        lightQueueSize = getInt("light-queue-size", lightQueueSize);
    }

    public boolean antiXray;
    public EngineMode engineMode;
    public int maxBlockHeight;
    public int updateRadius;
    public boolean lavaObscures;
    public boolean usePermission;
    public List<String> hiddenBlocks;
    public List<String> replacementBlocks;
    private void antiXray() {
        antiXray = getBoolean("anti-xray.enabled", false);
        engineMode = EngineMode.getById(getInt("anti-xray.engine-mode", EngineMode.HIDE.getId()));
        engineMode = engineMode == null ? EngineMode.HIDE : engineMode;
        maxBlockHeight = getInt("anti-xray.max-block-height", 64);
        updateRadius = getInt("anti-xray.update-radius", 2);
        lavaObscures = getBoolean("anti-xray.lava-obscures", false);
        usePermission = getBoolean("anti-xray.use-permission", false);
        hiddenBlocks = getList("anti-xray.hidden-blocks", Arrays.asList("copper_ore", "deepslate_copper_ore", "gold_ore", "deepslate_gold_ore", "iron_ore", "deepslate_iron_ore",
            "coal_ore", "deepslate_coal_ore", "lapis_ore", "deepslate_lapis_ore", "mossy_cobblestone", "obsidian", "chest", "diamond_ore", "deepslate_diamond_ore",
            "redstone_ore", "deepslate_redstone_ore", "clay", "emerald_ore", "deepslate_emerald_ore", "ender_chest"));
        replacementBlocks = getList("anti-xray.replacement-blocks", Arrays.asList("stone", "oak_planks", "deepslate"));
        if (PaperConfig.version < 19) {
            hiddenBlocks.remove("lit_redstone_ore");
            int index = replacementBlocks.indexOf("planks");
            if (index != -1) {
                replacementBlocks.set(index, "oak_planks");
            }
            set("anti-xray.hidden-blocks", hiddenBlocks);
            set("anti-xray.replacement-blocks", replacementBlocks);
        }
        log("Anti-Xray: " + (antiXray ? "enabled" : "disabled") + " / Engine Mode: " + engineMode.getDescription() + " / Up to " + ((maxBlockHeight >> 4) << 4) + " blocks / Update Radius: " + updateRadius);
        if (antiXray && usePermission) {
            Bukkit.getLogger().warning("You have enabled permission-based Anti-Xray checking - depending on your permission plugin, this may cause performance issues");
        }
    }

    public boolean altItemDespawnRateEnabled;
    public java.util.Map<net.minecraft.resources.ResourceLocation, Integer> altItemDespawnRateMap = new HashMap<>();
    private void altItemDespawnRate() {
        String path = "alt-item-despawn-rate";
        // Migrate from bukkit material to Mojang item ids
        if (PaperConfig.version < 26) {
            String world = worldName;
            try {
                org.bukkit.configuration.ConfigurationSection mapSection = config.getConfigurationSection("world-settings." + world + "." + path + ".items");
                if (mapSection == null) {
                    world = "default";
                    mapSection = config.getConfigurationSection("world-settings." + world + "." + path + ".items");
                }
                if (mapSection != null) {
                    for (String key : mapSection.getKeys(false)) {
                        int val = mapSection.getInt(key);
                        try {
                            // Ignore options that are already valid mojang wise, otherwise we might try to migrate the same config twice and fail.
                            boolean isMojangMaterial = net.minecraft.core.Registry.ITEM.getOptional(new net.minecraft.resources.ResourceLocation(key.toLowerCase())).isPresent();
                            mapSection.set(key, null);
                            String newKey = isMojangMaterial ? key.toLowerCase() : org.bukkit.Material.valueOf(key).getKey().getKey().toLowerCase();
                            mapSection.set(newKey, val);
                        } catch (Exception e) {
                            logError("Could not add item " + key + " to altItemDespawnRateMap: " + e.getMessage());
                        }
                    }
                    config.set("world-settings." + world + "." + path + ".items", mapSection);
                }
            } catch (Exception e) {
                logError("alt-item-despawn-rate was malformatted");
                return;
            }
        }

        altItemDespawnRateEnabled = getBoolean(path + ".enabled", false);

        if (config.getConfigurationSection("world-settings.default." + path + ".items") == null) {
            // Initialize default
            config.addDefault("world-settings.default." + path + ".items.cobblestone", 300);
        }

        if (!altItemDespawnRateEnabled) {
            return;
        }

        org.bukkit.configuration.ConfigurationSection mapSection = config.getConfigurationSection("world-settings." + worldName + "." + path + ".items");
        if (mapSection == null) {
            mapSection = config.getConfigurationSection("world-settings.default." + path + ".items");
        }
        if (mapSection != null) {
            for (String key : mapSection.getKeys(false)) {
                try {
                    int val = mapSection.getInt(key);
                    net.minecraft.resources.ResourceLocation keyLocation = new net.minecraft.resources.ResourceLocation(key);
                    if (net.minecraft.core.Registry.ITEM.getOptional(keyLocation).isPresent()) {
                        altItemDespawnRateMap.put(keyLocation, val);
                    } else {
                        logError("Could not add item " + key + " to altItemDespawnRateMap: not a valid item");
                    }
                } catch (Exception e) {
                    logError("Could not add item " + key + " to altItemDespawnRateMap: " + e.getMessage());
                }
            }
        }

        for (net.minecraft.resources.ResourceLocation key : altItemDespawnRateMap.keySet()) {
            log("Alternative item despawn rate of " + key.getPath() + ": " + altItemDespawnRateMap.get(key));
        }
    }

    public boolean perPlayerMobSpawns = false;
    private void perPlayerMobSpawns() {
        if (PaperConfig.version < 22) {
            set("per-player-mob-spawns", Boolean.TRUE);
        }
        perPlayerMobSpawns = getBoolean("per-player-mob-spawns", true);
    }

    public boolean enderDragonsDeathAlwaysPlacesDragonEgg = false;
    private void enderDragonsDeathAlwaysPlacesDragonEgg() {
        enderDragonsDeathAlwaysPlacesDragonEgg = getBoolean("ender-dragons-death-always-places-dragon-egg", enderDragonsDeathAlwaysPlacesDragonEgg);
    }

    public boolean updatePathfindingOnBlockUpdate = true;
    private void setUpdatePathfindingOnBlockUpdate() {
        updatePathfindingOnBlockUpdate = getBoolean("update-pathfinding-on-block-update", this.updatePathfindingOnBlockUpdate);
    }

    public boolean phantomIgnoreCreative = true;
    public boolean phantomOnlyAttackInsomniacs = true;
    private void phantomSettings() {
        phantomIgnoreCreative = getBoolean("phantoms-do-not-spawn-on-creative-players", phantomIgnoreCreative);
        phantomOnlyAttackInsomniacs = getBoolean("phantoms-only-attack-insomniacs", phantomOnlyAttackInsomniacs);
    }

    public boolean zombiesAlwaysCanPickUpLoot;
    public boolean skeletonsAlwaysCanPickUpLoot;
    private void setMobsAlwaysCanPickUpLoot() {
        zombiesAlwaysCanPickUpLoot = getBoolean("mobs-can-always-pick-up-loot.zombies", false);
        skeletonsAlwaysCanPickUpLoot = getBoolean("mobs-can-always-pick-up-loot.skeletons", false);
        log("Zombies can always pick up loot: " + zombiesAlwaysCanPickUpLoot + ". Skeletons can always pick up loot: " + skeletonsAlwaysCanPickUpLoot + ".");
    }

    public int expMergeMaxValue;
    private void expMergeMaxValue() {
        expMergeMaxValue = getInt("experience-merge-max-value", -1);
        log("Experience Merge Max Value: " + expMergeMaxValue);
    }

    public long delayChunkUnloadsBy;
    private void delayChunkUnloadsBy() {
        delayChunkUnloadsBy = PaperConfig.getSeconds(getString("delay-chunk-unloads-by", "10s"));
        if (delayChunkUnloadsBy > 0) {
            log("Delaying chunk unloads by " + delayChunkUnloadsBy + " seconds");
            delayChunkUnloadsBy *= 20;
        }
    }

    public boolean allowUsingSignsInsideSpawnProtection = false;
    private void allowUsingSignsInsideSpawnProtection() {
        allowUsingSignsInsideSpawnProtection = getBoolean("allow-using-signs-inside-spawn-protection", allowUsingSignsInsideSpawnProtection);
    }

    public int mapItemFrameCursorLimit = 128;
    private void mapItemFrameCursorLimit() {
        mapItemFrameCursorLimit = getInt("map-item-frame-cursor-limit", mapItemFrameCursorLimit);
    }

    public int mapItemFrameCursorUpdateInterval = 10;
    private void itemFrameCursorUpdateInterval() {
        mapItemFrameCursorUpdateInterval = getInt("map-item-frame-cursor-update-interval", mapItemFrameCursorUpdateInterval);
    }

    public boolean fixItemsMergingThroughWalls;
    private void fixItemsMergingThroughWalls() {
        fixItemsMergingThroughWalls = getBoolean("fix-items-merging-through-walls", fixItemsMergingThroughWalls);
    }

    public boolean fixInvulnerableEndCrystalExploit = true;
    private void fixInvulnerableEndCrystalExploit() {
        fixInvulnerableEndCrystalExploit = getBoolean("unsupported-settings.fix-invulnerable-end-crystal-exploit", fixInvulnerableEndCrystalExploit);
    }

    public boolean showSignClickCommandFailureMessagesToPlayer = false;
    private void showSignClickCommandFailureMessagesToPlayer() {
        showSignClickCommandFailureMessagesToPlayer = getBoolean("show-sign-click-command-failure-msgs-to-player", showSignClickCommandFailureMessagesToPlayer);
    }

    public boolean allowPlayerCrammingDamage = false;
    private void playerCrammingDamage() {
        allowPlayerCrammingDamage = getBoolean("allow-player-cramming-damage", allowPlayerCrammingDamage);
    }

    public boolean splitOverstackedLoot = true;
    private void splitOverstackedLoot() {
        splitOverstackedLoot = getBoolean("split-overstacked-loot", splitOverstackedLoot);
    }

    private com.google.common.collect.Table<String, String, Integer> sensorTickRates;
    private com.google.common.collect.Table<String, String, Integer> behaviorTickRates;
    private void tickRates() {
        config.addDefault("world-settings.default.tick-rates.sensor.villager.secondarypoisensor", 40);
        config.addDefault("world-settings.default.tick-rates.behavior.villager.validatenearbypoi", -1); // Example
        log("Tick rates:");
        sensorTickRates = loadTickRates("sensor");
        behaviorTickRates = loadTickRates("behavior");
    }

    public boolean hideItemmetaFromClients = false;
    public boolean hideDurabilityFromClients = false;
    private void getHideItemmetaFromClients() {
        hideItemmetaFromClients = getBoolean("anticheat.obfuscation.items.hide-itemmeta", hideItemmetaFromClients);
        hideDurabilityFromClients = getBoolean("anticheat.obfuscation.items.hide-durability", hideDurabilityFromClients);
    }

    private com.google.common.collect.Table<String, String, Integer> loadTickRates(String type) {
        log("  " + type + ":");
        com.google.common.collect.Table<String, String, Integer> table = com.google.common.collect.HashBasedTable.create();

        ConfigurationSection typeSection = config.getConfigurationSection("world-settings." + worldName + ".tick-rates." + type);
        if (typeSection == null) {
            typeSection = config.getConfigurationSection("world-settings.default.tick-rates." + type);
        }
        if (typeSection != null) {
            for (String entity : typeSection.getKeys(false)) {
                ConfigurationSection entitySection = typeSection.getConfigurationSection(entity);
                if (entitySection != null) {
                    log("    " + entity + ":");
                    for (String typeName : entitySection.getKeys(false)) {
                        if (entitySection.isInt(typeName)) {
                            int tickRate = entitySection.getInt(typeName);
                            table.put(entity.toLowerCase(Locale.ROOT), typeName.toLowerCase(Locale.ROOT), tickRate);
                            log("      " + typeName + ": " + tickRate);
                        }
                    }
                }
            }
        }

        if (table.isEmpty()) {
            log("    None configured");
        }
        return table;
    }

    public it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap<net.minecraft.resources.ResourceLocation> featureSeeds = new it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap<>();
    private void featureSeeds() {
        featureSeeds.defaultReturnValue(-1);
        final boolean randomise = getBoolean("feature-seeds.generate-random-seeds-for-all", false);
        final ConfigurationSection defaultSection = config.getConfigurationSection("world-settings.default.feature-seeds");
        final ConfigurationSection section = config.getConfigurationSection("world-settings." + worldName + ".feature-seeds");
        final net.minecraft.core.Registry<net.minecraft.world.level.levelgen.feature.ConfiguredFeature<?, ?>> registry
            = net.minecraft.server.MinecraftServer.getServer().registryAccess().registryOrThrow(net.minecraft.core.Registry.CONFIGURED_FEATURE_REGISTRY);
        if (section != null) {
            loadFeatureSeeds(section, registry);
        }

        // Also use default set seeds if not already set per world
        loadFeatureSeeds(defaultSection, registry);

        if (randomise) {
            final Map<String, Object> randomisedSeeds = new HashMap<>();
            final java.util.Random random = new java.security.SecureRandom();
            for (final net.minecraft.resources.ResourceLocation resourceLocation : registry.keySet()) {
                if (featureSeeds.containsKey(resourceLocation)) {
                    continue;
                }

                final long seed = random.nextLong();
                randomisedSeeds.put("world-settings." + worldName + ".feature-seeds." + resourceLocation.getPath(), seed);
                featureSeeds.put(resourceLocation, seed);
            }
            if (!randomisedSeeds.isEmpty()) {
                config.addDefaults(randomisedSeeds);
            }
        }
    }

    private void loadFeatureSeeds(final ConfigurationSection section, final net.minecraft.core.Registry<net.minecraft.world.level.levelgen.feature.ConfiguredFeature<?, ?>> registry) {
        for (final String key : section.getKeys(false)) {
            if (!(section.get(key) instanceof Number)) {
                continue;
            }

            final net.minecraft.resources.ResourceLocation location = new net.minecraft.resources.ResourceLocation(key);
            if (!registry.containsKey(location)) {
                logError("Invalid feature resource location: " + location);
                continue;
            }

            featureSeeds.putIfAbsent(location, section.getLong(key));
        }
    }

    public int getBehaviorTickRate(String typeName, String entityType, int def) {
        return getIntOrDefault(behaviorTickRates, typeName, entityType, def);
    }

    public int getSensorTickRate(String typeName, String entityType, int def) {
        return getIntOrDefault(sensorTickRates, typeName, entityType, def);
    }

    private int getIntOrDefault(com.google.common.collect.Table<String, String, Integer> table, String rowKey, String columnKey, int def) {
        Integer rate = table.get(columnKey, rowKey);
        return rate != null && rate > -1 ? rate : def;
    }

    public int maxBlockLightForMonsterSpawning = -1;
    private void minBlockLightForMobSpawning() {
        this.maxBlockLightForMonsterSpawning = getInt("monster-spawn-max-light-level", maxBlockLightForMonsterSpawning);
    }
}
