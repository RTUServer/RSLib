package org.bukkit.craftbukkit.v1_20_R3.block;

import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import org.bukkit.World;
import org.bukkit.block.Conduit;

public class CraftConduit extends CraftBlockEntityState<ConduitBlockEntity> implements Conduit {

    public CraftConduit(World world, ConduitBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftConduit(CraftConduit state) {
        super(state);
    }

    @Override
    public CraftConduit copy() {
        return new CraftConduit(this);
    }

    // Paper start - Conduit API
    @Override
    public boolean isActive() {
        requirePlaced();
        return this.getTileEntity().isActive();
    }

    @Override
    public int getRange() {
        requirePlaced();
        return this.getTileEntity().effectBlocks.size() / 7 * 16;
    }

    @Override
    public org.bukkit.entity.LivingEntity getTarget() {
        return this.getTileEntity().destroyTarget == null ? null : this.getTileEntity().destroyTarget.getBukkitLivingEntity();
    }
    // Paper end - Conduit API
}
