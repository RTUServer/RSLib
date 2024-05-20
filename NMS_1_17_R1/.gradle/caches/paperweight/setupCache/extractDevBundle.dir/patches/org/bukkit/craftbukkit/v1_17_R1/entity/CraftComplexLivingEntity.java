package org.bukkit.craftbukkit.v1_17_R1.entity;

import net.minecraft.world.entity.Mob;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.entity.ComplexLivingEntity;

public abstract class CraftComplexLivingEntity extends CraftMob implements ComplexLivingEntity { // Paper
    public CraftComplexLivingEntity(CraftServer server, Mob entity) { // Paper
        super(server, entity);
    }

    @Override
    public Mob getHandle() { // Paper
        return (Mob) entity; // Paper
    }

    @Override
    public String toString() {
        return "CraftComplexLivingEntity";
    }
}
