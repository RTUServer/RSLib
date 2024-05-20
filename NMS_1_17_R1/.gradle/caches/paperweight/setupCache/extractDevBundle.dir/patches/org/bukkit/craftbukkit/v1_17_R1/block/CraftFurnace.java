package org.bukkit.craftbukkit.v1_17_R1.block;

import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.bukkit.World;
import org.bukkit.block.Furnace;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftInventoryFurnace;
import org.bukkit.inventory.FurnaceInventory;

public abstract class CraftFurnace<T extends AbstractFurnaceBlockEntity> extends CraftContainer<T> implements Furnace {

    public CraftFurnace(World world, T tileEntity) {
        super(world, tileEntity);
    }

    @Override
    public FurnaceInventory getSnapshotInventory() {
        return new CraftInventoryFurnace(this.getSnapshot());
    }

    @Override
    public FurnaceInventory getInventory() {
        if (!this.isPlaced()) {
            return this.getSnapshotInventory();
        }

        return new CraftInventoryFurnace(this.getTileEntity());
    }

    @Override
    public short getBurnTime() {
        return (short) this.getSnapshot().litTime;
    }

    @Override
    public void setBurnTime(short burnTime) {
        this.getSnapshot().litTime = burnTime;
        // SPIGOT-844: Allow lighting and relighting using this API
        this.data = this.data.setValue(AbstractFurnaceBlock.LIT, burnTime > 0);
    }

    @Override
    public short getCookTime() {
        return (short) this.getSnapshot().cookingProgress;
    }

    @Override
    public void setCookTime(short cookTime) {
        this.getSnapshot().cookingProgress = cookTime;
    }

    @Override
    public int getCookTimeTotal() {
        return this.getSnapshot().cookingTotalTime;
    }

    @Override
    public void setCookTimeTotal(int cookTimeTotal) {
        this.getSnapshot().cookingTotalTime = cookTimeTotal;
    }

    // Paper start - cook speed multiplier API
    @Override
    public double getCookSpeedMultiplier() {
        return this.getSnapshot().cookSpeedMultiplier;
    }

    @Override
    public void setCookSpeedMultiplier(double multiplier) {
        com.google.common.base.Preconditions.checkArgument(multiplier >= 0, "Furnace speed multiplier cannot be negative");
        com.google.common.base.Preconditions.checkArgument(multiplier <= 200, "Furnace speed multiplier cannot more than 200");
        T snapshot = this.getSnapshot();
        snapshot.cookSpeedMultiplier = multiplier;
        snapshot.cookingTotalTime = AbstractFurnaceBlockEntity.getTotalCookTime(this.world.getHandle(), snapshot.recipeType, snapshot, snapshot.cookSpeedMultiplier); // Update the snapshot's current total cook time to scale with the newly set multiplier
    }
    // Paper end
}
