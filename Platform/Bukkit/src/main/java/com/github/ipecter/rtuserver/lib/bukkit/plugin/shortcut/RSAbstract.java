package com.github.ipecter.rtuserver.lib.bukkit.plugin.shortcut;

import com.github.ipecter.rtuserver.lib.bukkit.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.config.CommandConfiguration;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.config.MessageConfiguration;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.config.SettingConfiguration;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.util.format.ComponentFormatter;
import lombok.Data;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Data
public abstract class RSAbstract implements Message, Scheduler {

    private final RSPlugin plugin;
    private final SettingConfiguration setting;
    private final MessageConfiguration message;
    private final CommandConfiguration command;

    public RSAbstract(RSPlugin plugin) {
        this.plugin = plugin;
        this.setting = plugin.getConfigurations().getSetting();
        this.message = plugin.getConfigurations().getMessage();
        this.command = plugin.getConfigurations().getCommand();
    }

    protected void announce(CommandSender sender, String minimessage) {
        send(sender, message.getPrefix().append(ComponentFormatter.mini(minimessage)));
    }

    protected void announce(Player player, String minimessage) {
        send(player, message.getPrefix().append(ComponentFormatter.mini(minimessage)));
    }

    protected void announce(Audience audience, String minimessage) {
        send(audience, message.getPrefix().append(ComponentFormatter.mini(minimessage)));
    }

    protected void announce(CommandSender sender, Component component) {
        send(sender, message.getPrefix().append(component));
    }

    protected void announce(Player player, Component component) {
        send(player, message.getPrefix().append(component));
    }

    protected void announce(Audience audience, Component component) {
        send(audience, message.getPrefix().append(component));
    }

}