package io.papermc.paper.world.structure;

import io.papermc.paper.registry.PaperRegistry;
import io.papermc.paper.registry.RegistryKey;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import org.bukkit.NamespacedKey;
import org.bukkit.StructureType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Objects;
import java.util.function.Supplier;

@DefaultQualifier(NonNull.class)
public final class PaperConfiguredStructure {

    private PaperConfiguredStructure() {
    }

    public static void init() {
        new ConfiguredStructureRegistry().register();
    }

    static final class ConfiguredStructureRegistry extends PaperRegistry<ConfiguredStructure, ConfiguredStructureFeature<?, ?>> {

        private static final Supplier<Registry<StructureFeature<?>>> STRUCTURE_FEATURE_REGISTRY = registryFor(Registry.STRUCTURE_FEATURE_REGISTRY);

        public ConfiguredStructureRegistry() {
            super(RegistryKey.CONFIGURED_STRUCTURE_REGISTRY);
        }

        @Override
        public ConfiguredStructure convertToApi(NamespacedKey key, ConfiguredStructureFeature<?, ?> nms) {
            final ResourceLocation structureFeatureLoc = Objects.requireNonNull(STRUCTURE_FEATURE_REGISTRY.get().getKey(nms.feature));
            final StructureType structureType = Objects.requireNonNull(StructureType.getStructureTypes().get(structureFeatureLoc.getPath()), structureFeatureLoc + " could not be converted to an API type");
            return new ConfiguredStructure(key, structureType);
        }
    }
}
