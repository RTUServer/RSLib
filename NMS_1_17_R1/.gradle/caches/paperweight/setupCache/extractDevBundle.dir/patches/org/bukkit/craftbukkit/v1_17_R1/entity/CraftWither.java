package org.bukkit.craftbukkit.v1_17_R1.entity;

import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.boss.CraftBossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Wither;

public class CraftWither extends CraftMonster implements Wither, com.destroystokyo.paper.entity.CraftRangedEntity<WitherBoss> { // Paper

    private BossBar bossBar;

    public CraftWither(CraftServer server, WitherBoss entity) {
        super(server, entity);

        if (entity.bossEvent != null) {
            this.bossBar = new CraftBossBar(entity.bossEvent);
        }
    }

    @Override
    public WitherBoss getHandle() {
        return (WitherBoss) entity;
    }

    @Override
    public String toString() {
        return "CraftWither";
    }

    @Override
    public EntityType getType() {
        return EntityType.WITHER;
    }

    @Override
    public BossBar getBossBar() {
        return this.bossBar;
    }

    // Paper start
    @Override
    public boolean isCharged() {
        return getHandle().isPowered();
    }

    @Override
    public int getInvulnerableTicks() {
        return getHandle().getInvulnerableTicks();
    }

    @Override
    public void setInvulnerableTicks(int ticks) {
        getHandle().setInvulnerableTicks(ticks);
    }

    @Override
    public boolean canTravelThroughPortals() {
        return getHandle().canChangeDimensions();
    }

    @Override
    public void setCanTravelThroughPortals(boolean value) {
        getHandle().setCanTravelThroughPortals(value);
    }
    // Paper end
}
