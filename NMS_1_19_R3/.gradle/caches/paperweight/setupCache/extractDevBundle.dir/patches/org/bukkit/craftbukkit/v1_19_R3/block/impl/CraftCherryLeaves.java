/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.v1_19_R3.block.impl;

public final class CraftCherryLeaves extends org.bukkit.craftbukkit.v1_19_R3.block.data.CraftBlockData implements org.bukkit.block.data.type.Leaves, org.bukkit.block.data.Waterlogged {

    public CraftCherryLeaves() {
        super();
    }

    public CraftCherryLeaves(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.v1_19_R3.block.data.type.CraftLeaves

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty DISTANCE = getInteger(net.minecraft.world.level.block.CherryLeavesBlock.class, "distance");
    private static final net.minecraft.world.level.block.state.properties.BooleanProperty PERSISTENT = getBoolean(net.minecraft.world.level.block.CherryLeavesBlock.class, "persistent");

    @Override
    public boolean isPersistent() {
        return get(CraftCherryLeaves.PERSISTENT);
    }

    @Override
    public void setPersistent(boolean persistent) {
        set(CraftCherryLeaves.PERSISTENT, persistent);
    }

    @Override
    public int getDistance() {
        return get(CraftCherryLeaves.DISTANCE);
    }

    @Override
    public void setDistance(int distance) {
        set(CraftCherryLeaves.DISTANCE, distance);
    }

    // org.bukkit.craftbukkit.v1_19_R3.block.data.CraftWaterlogged

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty WATERLOGGED = getBoolean(net.minecraft.world.level.block.CherryLeavesBlock.class, "waterlogged");

    @Override
    public boolean isWaterlogged() {
        return get(CraftCherryLeaves.WATERLOGGED);
    }

    @Override
    public void setWaterlogged(boolean waterlogged) {
        set(CraftCherryLeaves.WATERLOGGED, waterlogged);
    }

    // Paper start
    @Override
    public int getMaximumDistance() {
        return getMax(DISTANCE);
    }

    @Override
    public int getMinimumDistance() {
        return getMin(DISTANCE);
    }
    // Paper end
}
