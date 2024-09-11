package com.github.ipecter.rtuserver.lib.bukkit.api.shortcut;

import com.github.ipecter.rtuserver.lib.bukkit.api.util.format.ComponentFormatter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

public interface Message {

    default void send(BukkitAudiences adventure, CommandSender sender, String minimessage) {
        Audience audience = adventure.sender(sender);
        audience.sendMessage(ComponentFormatter.mini(minimessage));
    }

    default void send(BukkitAudiences adventure, Player player, String minimessage) {
        Audience audience = adventure.player(player);
        audience.sendMessage(ComponentFormatter.mini(minimessage));
    }

    default void send(BukkitAudiences adventure, Audience audience, String minimessage) {
        audience.sendMessage(ComponentFormatter.mini(minimessage));
    }

    default void broadcast(BukkitAudiences adventure, String minimessage) {
        adventure.all().sendMessage(ComponentFormatter.mini(minimessage));
    }

    default void broadcast(BukkitAudiences adventure, Predicate<CommandSender> filter, String minimessage) {
        adventure.filter(filter).sendMessage(ComponentFormatter.mini(minimessage));
    }

    default void send(BukkitAudiences adventure, CommandSender sender, Component component) {
        Audience audience = adventure.sender(sender);
        audience.sendMessage(component);
    }

    default void send(BukkitAudiences adventure, Player player, Component component) {
        Audience audience = adventure.player(player);
        audience.sendMessage(component);
    }

    default void send(Audience audience, Component component) {
        audience.sendMessage(component);
    }

    default void broadcast(BukkitAudiences adventure, Component component) {
        adventure.all().sendMessage(component);
    }

    default void broadcast(BukkitAudiences adventure, Predicate<CommandSender> filter, Component component) {
        adventure.filter(filter).sendMessage(component);
    }

}
