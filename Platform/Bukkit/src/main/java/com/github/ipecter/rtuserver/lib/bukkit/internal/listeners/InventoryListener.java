package com.github.ipecter.rtuserver.lib.bukkit.internal.listeners;

import com.github.ipecter.rtuserver.lib.bukkit.RSLib;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.inventory.RSInventory;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.listener.RSListener;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.util.format.ComponentFormatter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class InventoryListener extends RSListener {

    public InventoryListener(RSPlugin plugin) {
        super(plugin);
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
                RSLib.getInstance().console(ComponentFormatter.mini("<red>인벤토리 클릭 이벤트를 사용하는 코드에서 결함이 발견되었습니다!</red>"));
                RSLib.getInstance().getAdventure().player(player).sendMessage(ComponentFormatter.mini("<red>인벤토리 클릭 이벤트를 사용하는 코드에서 결함이 발견되었습니다!</red>"));
                exception.printStackTrace();
            }
        }
    }

    @EventHandler
    private void onRSInventoryDrag(InventoryDragEvent e) {
        Inventory inv = e.getInventory();
        Player player = (Player) e.getWhoClicked();
        if (e.getView().getTopInventory().getHolder() instanceof RSInventory rsInventory) {
            boolean isPlayerInventory = inv != null && !(inv.getHolder() instanceof RSInventory);
            RSInventory.Event<InventoryDragEvent> event = new RSInventory.Event<>(e, inv, player, isPlayerInventory);
            RSInventory.Drag click = new RSInventory.Drag(e.getNewItems(), e.getCursor(), e.getOldCursor(), e.getType());
            try {
                e.setCancelled(!rsInventory.onDrag(event, click));
            } catch (Exception ex) {
                e.setCancelled(true);
                RSLib.getInstance().console(ComponentFormatter.mini("<red>인벤토리 드래그 이벤트를 사용하는 코드에서 결함이 발견되었습니다!</red>"));
                RSLib.getInstance().getAdventure().player(player).sendMessage(ComponentFormatter.mini("<red>인벤토리 드래그 이벤트를 사용하는 코드에서 결함이 발견되었습니다!</red>"));
                ex.printStackTrace();
            }
        }
    }

    @EventHandler
    private void onRSInventoryClose(InventoryCloseEvent e) {
        Inventory inv = e.getInventory();
        Player player = (Player) e.getPlayer();
        if (inv.getHolder() instanceof RSInventory rsInventory) {
            boolean isPlayerInventory = !(inv.getHolder() instanceof RSInventory);
            RSInventory.Event<InventoryCloseEvent> event = new RSInventory.Event<>(e, inv, player, isPlayerInventory);
            RSInventory.Close close = new RSInventory.Close(e.getReason());
            rsInventory.onClose(event, close);
        }
    }

}
