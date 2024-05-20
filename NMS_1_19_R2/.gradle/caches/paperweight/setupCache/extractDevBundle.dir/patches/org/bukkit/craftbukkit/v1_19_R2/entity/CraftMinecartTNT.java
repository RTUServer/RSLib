package org.bukkit.craftbukkit.v1_19_R2.entity;

import net.minecraft.world.entity.vehicle.MinecartTNT;
import org.bukkit.craftbukkit.v1_19_R2.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.minecart.ExplosiveMinecart;

public final class CraftMinecartTNT extends CraftMinecart implements ExplosiveMinecart { // Paper - getHandle -> make public
    CraftMinecartTNT(CraftServer server, MinecartTNT entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "CraftMinecartTNT";
    }

    @Override
    public EntityType getType() {
        return EntityType.MINECART_TNT;
    }
    // Paper start
    @Override
    public net.minecraft.world.entity.vehicle.MinecartTNT getHandle() {
        return (net.minecraft.world.entity.vehicle.MinecartTNT) entity;
    }

    @Override
    public void setFuseTicks(int fuseTicks) {
        this.getHandle().fuse = fuseTicks;
    }

    @Override
    public int getFuseTicks() {
        return this.getHandle().getFuse();
    }

    @Override
    public boolean isIgnited() {
        return this.getHandle().isPrimed();
    }

    @Override
    public void ignite() {
        this.getHandle().primeFuse();
    }

    @Override
    public void explode() {
        explode(this.getHandle().getDeltaMovement().horizontalDistanceSqr());
    }

    @Override
    public void explode(double power) {
        com.google.common.base.Preconditions.checkArgument(power >= 0 && Double.isFinite(power), "Explosion power must be a finite non-negative number");
        this.getHandle().explode(power);
    }
    // Paper end
}
