package com.github.ipecter.rtuserver.lib.plugin.listener;

import com.github.ipecter.rtuserver.lib.bukkit.RSLib;
import com.github.ipecter.rtuserver.lib.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.plugin.config.CommandConfiguration;
import com.github.ipecter.rtuserver.lib.plugin.config.MessageConfiguration;
import com.github.ipecter.rtuserver.lib.bukkit.util.common.ComponentUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

@Getter
public abstract class RSListener implements Listener {

    private final RSPlugin plugin;
    private final MessageConfiguration message;
    private final CommandConfiguration command;

    public RSListener(RSPlugin plugin) {
        this.plugin = plugin;
        this.message = plugin.getConfigurations().getMessage();
        this.command = plugin.getConfigurations().getCommand();
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
            component = component.hoverEvent(HoverEvent.showText(lore));
        }
        plugin.getAdventure().sender(sender).sendMessage(component);
    }

    public void sendMessage(CommandSender sender, String miniMessage) {
        sendMessage(sender, ComponentUtil.formatted(sender, miniMessage));
    }
}
