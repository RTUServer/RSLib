package com.github.ipecter.rtuserver.lib.plugin.inventory;

import com.github.ipecter.rtuserver.lib.bukkit.RSLib;
import com.github.ipecter.rtuserver.lib.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.plugin.config.CommandConfiguration;
import com.github.ipecter.rtuserver.lib.plugin.config.MessageConfiguration;
import com.github.ipecter.rtuserver.lib.plugin.listener.RSListener;
import com.github.ipecter.rtuserver.lib.bukkit.util.common.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public abstract class RSInventory extends RSListener implements InventoryHolder {

    private final RSPlugin plugin;
    private final MessageConfiguration message;
    private final CommandConfiguration command;

    public RSInventory(RSPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
        this.message = plugin.getConfigurations().getMessage();
        this.command = plugin.getConfigurations().getCommand();
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
