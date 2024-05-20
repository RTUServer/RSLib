package com.destroystokyo.paper.loottable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftNamespacedKey;

public class PaperMinecartLootableInventory implements PaperLootableEntityInventory {

    private AbstractMinecartContainer entity;

    public PaperMinecartLootableInventory(AbstractMinecartContainer entity) {
        this.entity = entity;
    }

    @Override
    public org.bukkit.loot.LootTable getLootTable() {
        return entity.lootTable != null && !entity.lootTable.getPath().isEmpty() ? Bukkit.getLootTable(CraftNamespacedKey.fromMinecraft(entity.lootTable)) : null;
    }

    @Override
    public void setLootTable(org.bukkit.loot.LootTable table, long seed) {
        setLootTable(table);
        setSeed(seed);
    }

    @Override
    public void setSeed(long seed) {
        entity.lootTableSeed = seed;
    }

    @Override
    public long getSeed() {
        return entity.lootTableSeed;
    }

    @Override
    public void setLootTable(org.bukkit.loot.LootTable table) {
        entity.lootTable = (table == null) ? null : CraftNamespacedKey.toMinecraft(table.getKey());
    }

    @Override
    public PaperLootableInventoryData getLootableData() {
        return entity.lootableData;
    }

    @Override
    public Entity getHandle() {
        return entity;
    }

    @Override
    public LootableInventory getAPILootableInventory() {
        return (LootableInventory) entity.getBukkitEntity();
    }

    @Override
    public Level getNMSWorld() {
        return entity.level;
    }
}
