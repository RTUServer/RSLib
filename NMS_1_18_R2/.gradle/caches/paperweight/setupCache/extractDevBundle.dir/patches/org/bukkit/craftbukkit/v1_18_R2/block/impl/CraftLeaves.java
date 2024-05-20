/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.v1_18_R2.block.impl;

public final class CraftLeaves extends org.bukkit.craftbukkit.v1_18_R2.block.data.CraftBlockData implements org.bukkit.block.data.type.Leaves {

    public CraftLeaves() {
        super();
    }

    public CraftLeaves(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.v1_18_R2.block.data.type.CraftLeaves

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty DISTANCE = getInteger(net.minecraft.world.level.block.LeavesBlock.class, "distance");
    private static final net.minecraft.world.level.block.state.properties.BooleanProperty PERSISTENT = getBoolean(net.minecraft.world.level.block.LeavesBlock.class, "persistent");

    @Override
    public boolean isPersistent() {
        return get(CraftLeaves.PERSISTENT);
    }

    @Override
    public void setPersistent(boolean persistent) {
        set(CraftLeaves.PERSISTENT, persistent);
    }

    @Override
    public int getDistance() {
        return get(CraftLeaves.DISTANCE);
    }

    @Override
    public void setDistance(int distance) {
        set(CraftLeaves.DISTANCE, distance);
    }

    // Paper start
    @Override
    public int getMaximumDistance() {
        return getMax(CraftLeaves.DISTANCE);
    }

    @Override
    public int getMinimumDistance() {
        return getMin(CraftLeaves.DISTANCE);
    }
    // Paper end
}
