package org.bukkit.craftbukkit.v1_18_R1.inventory;

import net.minecraft.world.Container;
import org.bukkit.inventory.SaddledHorseInventory;

public class CraftSaddledInventory extends CraftInventoryAbstractHorse implements SaddledHorseInventory {

    public CraftSaddledInventory(Container inventory) {
        super(inventory);
    }

}
