package com.github.ipecter.rtuserver.lib.plugin.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class RSInventory implements InventoryHolder, Listener {

    public boolean onClick(Event event, Click click) {
        return true;
    }

    public void onClose(Event event) {
    }

    public record Event(Inventory inventory, Player player, boolean isInventory) {
    }

    public record Click(int slot, InventoryType.SlotType slotType, ClickType type) {
    }

}
