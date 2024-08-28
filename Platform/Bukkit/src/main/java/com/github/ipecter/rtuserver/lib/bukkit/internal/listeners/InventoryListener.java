package com.github.ipecter.rtuserver.lib.bukkit.internal.listeners;

import com.github.ipecter.rtuserver.lib.bukkit.RSLib;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.inventory.RSInventory;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.listener.RSListener;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.util.format.ComponentFormatter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class InventoryListener extends RSListener {
    
    private final RSLib lib;

    public InventoryListener(RSLib plugin) {
        super(plugin);
        lib = plugin;
    }

    @EventHandler
    private void onRSInventoryClick(InventoryClickEvent e) {
        Inventory inv = e.getClickedInventory();
        Player player = (Player) e.getWhoClicked();
        if (e.getView().getTopInventory().getHolder() instanceof RSInventory rsInventory) {
            boolean isPlayerInventory = inv != null && !(inv.getHolder() instanceof RSInventory);
            RSInventory.Event<InventoryClickEvent> event = new RSInventory.Event<>(e, inv, player, isPlayerInventory);
            RSInventory.Click click = new RSInventory.Click(e.getSlot(), e.getSlotType(), e.getClick());
            try {
                e.setCancelled(!rsInventory.onClick(event, click));
            } catch (Exception exception) {
                e.setCancelled(true);
                Component errorMessage = ComponentFormatter.mini(getMessage().get("error.inventory"));
                lib.console(errorMessage);
                lib.getAdventure().player(player).sendMessage(errorMessage);
                exception.printStackTrace();
            }
        }
    }

    @EventHandler
    private void onRSInventoryDrag(InventoryDragEvent event) {
        Inventory inv = event.getInventory();
        Player player = (Player) event.getWhoClicked();
        if (event.getView().getTopInventory().getHolder() instanceof RSInventory rsInventory) {
            boolean isPlayerInventory = inv != null && !(inv.getHolder() instanceof RSInventory);
            RSInventory.Event<InventoryDragEvent> holderEvent = new RSInventory.Event<>(event, inv, player, isPlayerInventory);
            RSInventory.Drag click = new RSInventory.Drag(event.getNewItems(), event.getCursor(), event.getOldCursor(), event.getType());
            try {
                event.setCancelled(!rsInventory.onDrag(holderEvent, click));
            } catch (Exception ex) {
                event.setCancelled(true);
                Component errorMessage = ComponentFormatter.mini(getMessage().get("error.inventory"));
                lib.console(errorMessage);
                lib.getAdventure().player(player).sendMessage(errorMessage);
                ex.printStackTrace();
            }
        }
    }

    @EventHandler
    private void onRSInventoryClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        Player player = (Player) event.getPlayer();
        if (inv.getHolder() instanceof RSInventory rsInventory) {
            boolean isPlayerInventory = !(inv.getHolder() instanceof RSInventory);
            RSInventory.Event<InventoryCloseEvent> holderEvent = new RSInventory.Event<>(event, inv, player, isPlayerInventory);
            RSInventory.Close close = new RSInventory.Close(event.getReason());
            rsInventory.onClose(holderEvent, close);
        }
    }

}
