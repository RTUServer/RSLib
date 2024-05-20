package org.bukkit.craftbukkit.v1_18_R2.entity;

import com.google.common.base.Preconditions;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
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
    // Paper start - Panda API
    @Override
    public void setSneezeTicks(int ticks) {
        this.getHandle().setSneezeCounter(ticks);
    }

    @Override
    public int getSneezeTicks() {
        return this.getHandle().getSneezeCounter();
    }

    @Override
    public void setSneezing(boolean sneeze) {
        this.getHandle().sneeze(sneeze);
    }

    @Override
    public boolean isSneezing() {
        return this.getHandle().isSneezing();
    }

    @Override
    public void setEatingTicks(int ticks) {
        this.getHandle().setEatCounter(ticks);
    }

    @Override
    public int getEatingTicks() {
        return this.getHandle().getEatCounter();
    }

    @Override
    public void setUnhappyTicks(int ticks) {
        this.getHandle().setUnhappyCounter(ticks);
    }

    @Override
    public int getUnhappyTicks() {
        return this.getHandle().getUnhappyCounter();
    }

    @Override
    public boolean isRolling() {
        return this.getHandle().isRolling();
    }

    @Override
    public void setRolling(boolean rolling) {
        this.getHandle().roll(rolling);
    }

    @Override
    public boolean isOnBack() {
        return this.getHandle().isOnBack();
    }

    @Override
    public void setIsOnBack(boolean onBack) {
        this.getHandle().setOnBack(onBack);
    }

    @Override
    public boolean isSitting() {
        return this.getHandle().isSitting();
    }

    @Override
    public void setSitting(boolean sitting) {
        this.getHandle().sit(sitting);
    }
    // Paper end - Panda API

    public static Gene fromNms(net.minecraft.world.entity.animal.Panda.Gene gene) {
        Preconditions.checkArgument(gene != null, "Gene may not be null");

        return Gene.values()[gene.ordinal()];
    }

    public static net.minecraft.world.entity.animal.Panda.Gene toNms(Gene gene) {
        Preconditions.checkArgument(gene != null, "Gene may not be null");

        return net.minecraft.world.entity.animal.Panda.Gene.values()[gene.ordinal()];
    }
}
