package com.github.ipecter.rtuserver.lib.plugin.inventory;

import com.github.ipecter.rtuserver.lib.RSLib;
import com.github.ipecter.rtuserver.lib.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.plugin.listener.RSListener;
import com.github.ipecter.rtuserver.lib.util.common.ComponentUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class RSInventoryListener extends RSListener {

    public RSInventoryListener(RSPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    private void onClick(InventoryClickEvent e) {
        Inventory inv = e.getClickedInventory();
        Player player = (Player) e.getWhoClicked();
        if (e.getView().getTopInventory().getHolder() instanceof RSInventory rsInventory) {
            boolean isPlayerInventory = inv != null && !(inv.getHolder() instanceof RSInventory);
            RSInventory.Event event = new RSInventory.Event(e, inv, player, isPlayerInventory);
            RSInventory.Click click = new RSInventory.Click(e, e.getSlot(), e.getSlotType(), e.getClick());
            try {
                e.setCancelled(!rsInventory.onClick(event, click));
            } catch (Exception exception) {
                e.setCancelled(true);
                RSLib.getInstance().console(ComponentUtil.miniMessage("<red>인벤토리 클릭 이벤트를 사용하는 코드에서 결함이 발견되었습니다!</red>"));
                RSLib.getInstance().getAdventure().player(player).sendMessage(ComponentUtil.miniMessage("<red>인벤토리 클릭 이벤트를 사용하는 코드에서 결함이 발견되었습니다!</red>"));
                exception.printStackTrace();
            }
        }
    }

    @EventHandler
    private void onDrag(InventoryDragEvent e) {
        Inventory inv = e.getInventory();
        Player player = (Player) e.getWhoClicked();
        if (e.getView().getTopInventory().getHolder() instanceof RSInventory rsInventory) {
            boolean isPlayerInventory = inv != null && !(inv.getHolder() instanceof RSInventory);
            RSInventory.Event event = new RSInventory.Event(e, inv, player, isPlayerInventory);
            RSInventory.Drag click = new RSInventory.Drag(e, e.getNewItems(), e.getCursor(), e.getOldCursor(), e.getType());
            try {
                e.setCancelled(!rsInventory.onDrag(event, click));
            } catch (Exception exception) {
                e.setCancelled(true);
                RSLib.getInstance().console(ComponentUtil.miniMessage("<red>인벤토리 드래그 이벤트를 사용하는 코드에서 결함이 발견되었습니다!</red>"));
                RSLib.getInstance().getAdventure().player(player).sendMessage(ComponentUtil.miniMessage("<red>인벤토리 드래그 이벤트를 사용하는 코드에서 결함이 발견되었습니다!</red>"));
                exception.printStackTrace();
            }
        }
    }

    @EventHandler
    private void onClose(InventoryCloseEvent e) {
        Inventory inv = e.getInventory();
        Player player = (Player) e.getPlayer();
        if (inv.getHolder() instanceof RSInventory rsInventory) {
            boolean isPlayerInventory = !(inv.getHolder() instanceof RSInventory);
            RSInventory.Event event = new RSInventory.Event(e, inv, player, isPlayerInventory);
            RSInventory.Close close = new RSInventory.Close(e, e.getReason());
            rsInventory.onClose(event);
        }
    }

}
