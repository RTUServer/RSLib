package org.bukkit.craftbukkit.v1_20_R3.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.potion.CraftPotionEffectType;
import org.bukkit.craftbukkit.v1_20_R3.potion.CraftPotionUtil;
import org.bukkit.entity.MushroomCow;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CraftMushroomCow extends CraftCow implements MushroomCow, io.papermc.paper.entity.PaperShearable { // Paper
    public CraftMushroomCow(CraftServer server, net.minecraft.world.entity.animal.MushroomCow entity) {
        super(server, entity);
    }

    @Override
    public boolean hasEffectsForNextStew() {
        return this.getHandle().stewEffects != null && !this.getHandle().stewEffects.isEmpty();
    }

    @Override
    public List<PotionEffect> getEffectsForNextStew() {
        if (this.hasEffectsForNextStew()) {
            return this.getHandle().stewEffects.stream().map(recordSuspiciousEffect -> CraftPotionUtil.toBukkit(recordSuspiciousEffect.createEffectInstance())).toList();
        }
        return ImmutableList.of();
    }

    // Paper start - add overloads to use suspicious effect entry to mushroom cow and suspicious stew meta
    @Override
    public boolean addEffectToNextStew(PotionEffect potionEffect, boolean overwrite) {
        Preconditions.checkArgument(potionEffect != null, "PotionEffect cannot be null");
        return addEffectToNextStew(io.papermc.paper.potion.SuspiciousEffectEntry.create(potionEffect.getType(), potionEffect.getDuration()), overwrite);
    }

    @Override
    public boolean addEffectToNextStew(io.papermc.paper.potion.SuspiciousEffectEntry suspiciousEffectEntry, boolean overwrite) {
        Preconditions.checkArgument(suspiciousEffectEntry != null, "SuspiciousEffectEntry cannot be null");
        MobEffect minecraftPotionEffect = CraftPotionEffectType.bukkitToMinecraft(suspiciousEffectEntry.effect());
        if (!overwrite && this.hasEffectForNextStew(suspiciousEffectEntry.effect())) {
            return false;
        }
        SuspiciousEffectHolder.EffectEntry recordSuspiciousEffect = new SuspiciousEffectHolder.EffectEntry(minecraftPotionEffect, suspiciousEffectEntry.duration());
        this.removeEffectFromNextStew(suspiciousEffectEntry.effect()); // Avoid duplicates of effects
        // Paper start - fix modification of immutable stew effects list
        if (this.getHandle().stewEffects == null) {
            this.getHandle().stewEffects = List.of(recordSuspiciousEffect);
        } else {
            this.getHandle().stewEffects = io.papermc.paper.util.MCUtil.copyListAndAdd(this.getHandle().stewEffects, recordSuspiciousEffect);
        }
        // Paper end - fix modification of immutable stew effects list
        return true;
    }
    // Paper end - add overloads to use suspicious effect entry to mushroom cow and suspicious stew meta

    @Override
    public boolean removeEffectFromNextStew(PotionEffectType potionEffectType) {
        Preconditions.checkArgument(potionEffectType != null, "potionEffectType cannot be null");
        if (!this.hasEffectsForNextStew()) {
            return false;
        }
        MobEffect minecraftPotionEffectType = CraftPotionEffectType.bukkitToMinecraft(potionEffectType);
        // Paper start - fix modification of immutable stew effects list
        if (this.getHandle().stewEffects == null) return false;

        final int oldSize = this.getHandle().stewEffects.size();
        this.getHandle().stewEffects = io.papermc.paper.util.MCUtil.copyListAndRemoveIf(
            this.getHandle().stewEffects, s -> java.util.Objects.equals(s.effect(), minecraftPotionEffectType)
        );

        final int newSize = this.getHandle().stewEffects.size();
        if (newSize == 0) {
            this.getHandle().stewEffects = null; // Null the empty list, mojang expect this
        }

        return oldSize != newSize; // Yield back if the size changed, implying an object was removed.
        // Paper end - fix modification of immutable stew effects list
    }

    @Override
    public boolean hasEffectForNextStew(PotionEffectType potionEffectType) {
        Preconditions.checkArgument(potionEffectType != null, "potionEffectType cannot be null");
        if (!this.hasEffectsForNextStew()) {
            return false;
        }
        MobEffect minecraftPotionEffectType = CraftPotionEffectType.bukkitToMinecraft(potionEffectType);
        return this.getHandle().stewEffects.stream().anyMatch(recordSuspiciousEffect -> recordSuspiciousEffect.effect().equals(minecraftPotionEffectType));
    }

    @Override
    public void clearEffectsForNextStew() {
        this.getHandle().stewEffects = null;
    }

    @Override
    public net.minecraft.world.entity.animal.MushroomCow getHandle() {
        return (net.minecraft.world.entity.animal.MushroomCow) this.entity;
    }

    @Override
    public Variant getVariant() {
        return Variant.values()[this.getHandle().getVariant().ordinal()];
    }

    @Override
    public void setVariant(Variant variant) {
        Preconditions.checkArgument(variant != null, "Variant cannot be null");

        this.getHandle().setVariant(net.minecraft.world.entity.animal.MushroomCow.MushroomType.values()[variant.ordinal()]);
    }

    // Paper start
    @Override
    public java.util.List<io.papermc.paper.potion.SuspiciousEffectEntry> getStewEffects() {
        if (this.getHandle().stewEffects == null) {
            return java.util.List.of();
        }

        java.util.List<io.papermc.paper.potion.SuspiciousEffectEntry> nmsPairs = new java.util.ArrayList<>(this.getHandle().stewEffects.size());
        for (final net.minecraft.world.level.block.SuspiciousEffectHolder.EffectEntry effect : this.getHandle().stewEffects) {
            nmsPairs.add(io.papermc.paper.potion.SuspiciousEffectEntry.create(
                org.bukkit.craftbukkit.v1_20_R3.potion.CraftPotionEffectType.minecraftToBukkit(effect.effect()),
                effect.duration()
            ));
        }

        return java.util.Collections.unmodifiableList(nmsPairs);
    }

    @Override
    public void setStewEffects(final java.util.List<io.papermc.paper.potion.SuspiciousEffectEntry> effects) {
        if (effects.isEmpty()) {
            this.getHandle().stewEffects = null;
            return;
        }

        java.util.List<net.minecraft.world.level.block.SuspiciousEffectHolder.EffectEntry> nmsPairs = new java.util.ArrayList<>(effects.size());
        for (final io.papermc.paper.potion.SuspiciousEffectEntry effect : effects) {
            nmsPairs.add(new net.minecraft.world.level.block.SuspiciousEffectHolder.EffectEntry(
                org.bukkit.craftbukkit.v1_20_R3.potion.CraftPotionEffectType.bukkitToMinecraft(effect.effect()),
                effect.duration()
            ));
        }

        this.getHandle().stewEffects = nmsPairs;
    }
    // Paper end

    @Override
    public String toString() {
        return "CraftMushroomCow";
    }
}
