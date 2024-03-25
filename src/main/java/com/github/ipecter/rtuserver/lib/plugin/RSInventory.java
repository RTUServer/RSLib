package com.github.ipecter.rtuserver.lib.plugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class RSInventory implements InventoryHolder, Listener {

    /***
     *
     * @param event
     * @param click
     * @return false = 이벤트가 캔슬됩니다
     */
    public boolean onClick(Event event, Click click) {
        return true;
    }

    public void onClose(Event event) {
    }

    public record Event(Inventory inventory, Player player, boolean isPlayerInventory) {
    }

    public record Click(int slot, InventoryType.SlotType slotType, ClickType type) {
    }
    
}
