package org.bukkit.craftbukkit.v1_17_R1.block.data.type;

import org.bukkit.block.data.type.Lantern;
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData;

public abstract class CraftLantern extends CraftBlockData implements Lantern {

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty HANGING = getBoolean("hanging");

    @Override
    public boolean isHanging() {
        return get(CraftLantern.HANGING);
    }

    @Override
    public void setHanging(boolean hanging) {
        set(CraftLantern.HANGING, hanging);
    }
}
