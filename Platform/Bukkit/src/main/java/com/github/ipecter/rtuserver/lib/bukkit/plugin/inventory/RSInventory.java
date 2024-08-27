package com.github.ipecter.rtuserver.lib.bukkit.plugin.inventory;

import com.github.ipecter.rtuserver.lib.bukkit.plugin.shortcut.RSAbstract;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.RSPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public abstract class RSInventory extends RSAbstract implements InventoryHolder {

    public RSInventory(RSPlugin plugin) {
        super(plugin);
    }

    public boolean onClick(Event<InventoryClickEvent> event, Click click) {
        return true;
    }

    public boolean onDrag(Event<InventoryDragEvent> event, Drag drag) {
        return true;
    }

    public void onClose(Event<InventoryCloseEvent> event, Close close) {
    }

    public record Event<T extends InventoryEvent>(T event, Inventory inventory, Player player, boolean isInventory) {
    }

    public record Drag(Map<Integer, ItemStack> items, ItemStack cursor, ItemStack oldCursor, DragType type) {
    }

    public record Click(int slot, InventoryType.SlotType slotType, ClickType type) {
    }

    public record Close(InventoryCloseEvent.Reason reason) {
    }
}
