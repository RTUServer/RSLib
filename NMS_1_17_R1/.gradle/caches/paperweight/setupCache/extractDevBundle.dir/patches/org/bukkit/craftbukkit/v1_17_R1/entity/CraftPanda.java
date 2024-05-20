package org.bukkit.craftbukkit.v1_17_R1.entity;

import com.google.common.base.Preconditions;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Panda.Gene;

public class CraftPanda extends CraftAnimals implements Panda {

    public CraftPanda(CraftServer server, net.minecraft.world.entity.animal.Panda entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.animal.Panda getHandle() {
        return (net.minecraft.world.entity.animal.Panda) super.getHandle();
    }

    @Override
    public EntityType getType() {
        return EntityType.PANDA;
    }

    @Override
    public String toString() {
        return "CraftPanda";
    }

    @Override
    public Gene getMainGene() {
        return CraftPanda.fromNms(this.getHandle().getMainGene());
    }

    @Override
    public void setMainGene(Gene gene) {
        this.getHandle().setMainGene(CraftPanda.toNms(gene));
    }

    @Override
    public Gene getHiddenGene() {
        return CraftPanda.fromNms(this.getHandle().getHiddenGene());
    }

    @Override
    public void setHiddenGene(Gene gene) {
        this.getHandle().setHiddenGene(CraftPanda.toNms(gene));
    }

    public static Gene fromNms(net.minecraft.world.entity.animal.Panda.Gene gene) {
        Preconditions.checkArgument(gene != null, "Gene may not be null");

        return Gene.values()[gene.ordinal()];
    }

    public static net.minecraft.world.entity.animal.Panda.Gene toNms(Gene gene) {
        Preconditions.checkArgument(gene != null, "Gene may not be null");

        return net.minecraft.world.entity.animal.Panda.Gene.values()[gene.ordinal()];
    }
}
