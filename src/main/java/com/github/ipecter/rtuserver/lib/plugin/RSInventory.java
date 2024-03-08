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

    @EventHandler
    private void onClick(InventoryClickEvent e) {
        Inventory inv = e.getClickedInventory();
        Player player = (Player) e.getWhoClicked();
        boolean isInventory = !(inv.getHolder() instanceof RSInventory);
        Event event = new Event(inv, player, isInventory);
        Click click = new Click(e.getSlot(), e.getSlotType(), e.getClick());
        onClick(event, click);
    }

    @EventHandler
    private void onClose(InventoryCloseEvent e) {
        Inventory inv = e.getInventory();
        Player player = (Player) e.getPlayer();
        boolean isInventory = !(inv.getHolder() instanceof RSInventory);
        Event event = new Event(inv, player, isInventory);
        onClose(event);
    }

    public void onClick(Event event, Click click) {
    }

    public void onClose(Event event) {
    }

    public record Event(Inventory inventory, Player player, boolean isInventory) {
    }

    public record Click(int slot, InventoryType.SlotType slotType, ClickType type) {
    }

}
