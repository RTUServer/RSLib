package com.github.ipecter.rtuserver.lib.plugin.inventory;

import com.github.ipecter.rtuserver.lib.plugin.RSInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class RSInventoryListener implements Listener {

    @EventHandler
    private void onClick(InventoryClickEvent e) {
        Inventory inv = e.getClickedInventory();
        Player player = (Player) e.getWhoClicked();
        if (e.getView().getTopInventory().getHolder() instanceof RSInventory rsInventory) {
            boolean isPlayerInventory = inv != null && !(inv.getHolder() instanceof RSInventory);
            RSInventory.Event event = new RSInventory.Event(inv, player, isPlayerInventory);
            RSInventory.Click click = new RSInventory.Click(e.getSlot(), e.getSlotType(), e.getClick());
            rsInventory.onClick(event, click);
        }
    }

    @EventHandler
    private void onClose(InventoryCloseEvent e) {
        Inventory inv = e.getInventory();
        Player player = (Player) e.getPlayer();
        if (inv.getHolder() instanceof RSInventory rsInventory) {
            boolean isPlayerInventory = !(inv.getHolder() instanceof RSInventory);
            RSInventory.Event event = new RSInventory.Event(inv, player, isPlayerInventory);
            rsInventory.onClose(event);
        }
    }

}
