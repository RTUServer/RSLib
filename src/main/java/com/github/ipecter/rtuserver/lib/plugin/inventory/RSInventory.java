package com.github.ipecter.rtuserver.lib.plugin.inventory;

import com.github.ipecter.rtuserver.lib.RSLib;
import com.github.ipecter.rtuserver.lib.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.plugin.config.CommandConfiguration;
import com.github.ipecter.rtuserver.lib.plugin.config.MessageConfiguration;
import com.github.ipecter.rtuserver.lib.util.common.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class RSInventory implements InventoryHolder, Listener {

    private final RSPlugin plugin;
    private final MessageConfiguration message;
    private final CommandConfiguration command;

    public RSInventory(RSPlugin plugin) {
        this.plugin = plugin;
        this.message = plugin.getConfigurations().getMessage();
        this.command = plugin.getConfigurations().getCommand();
    }

    public boolean onClick(Event event, Click click) {
        return true;
    }

    public void onClose(Event event) {
    }

    public record Event(Inventory inventory, Player player, boolean isInventory) {
    }

    public record Click(int slot, InventoryType.SlotType slotType, ClickType type) {
    }

    public boolean isOp(CommandSender sender) {
        return sender.isOp();
    }

    public boolean hasPermission(CommandSender sender, String node) {
        return sender.hasPermission(node);
    }

    public void sendAnnounce(CommandSender sender, Component component) {
        sendMessage(sender, message.getPrefix().append(component));
    }

    public void sendAnnounce(CommandSender sender, String miniMessage) {
        sendAnnounce(sender, ComponentUtil.formatted(sender, miniMessage));
    }

    public void sendMessage(CommandSender sender, Component component) {
        if (component.hoverEvent() == null) {
            Component lore = ComponentUtil.formatted(sender, RSLib.getInstance().getModules().getSystemMessageModule().getLore());
            component.
        }
        plugin.getAdventure().sender(sender).sendMessage(component);
    }

    public void sendMessage(CommandSender sender, String miniMessage) {
        sendMessage(sender, ComponentUtil.formatted(sender, miniMessage));
    }
}
