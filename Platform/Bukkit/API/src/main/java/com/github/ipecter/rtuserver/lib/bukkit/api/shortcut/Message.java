package com.github.ipecter.rtuserver.lib.bukkit.api.shortcut;

import com.github.ipecter.rtuserver.lib.bukkit.RSLib;
import com.github.ipecter.rtuserver.lib.bukkit.api.util.format.ComponentFormatter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

public interface Message {


    default void send(CommandSender sender, String minimessage) {
        Audience audience = RSLib.getInstance().getAdventure().sender(sender);
        audience.sendMessage(ComponentFormatter.mini(minimessage));
    }

    default void send(Player player, String minimessage) {
        Audience audience = RSLib.getInstance().getAdventure().player(player);
        audience.sendMessage(ComponentFormatter.mini(minimessage));
    }

    default void send(Audience audience, String minimessage) {
        audience.sendMessage(ComponentFormatter.mini(minimessage));
    }

    default void broadcast(String minimessage) {
        RSLib.getInstance().getAdventure().all().sendMessage(ComponentFormatter.mini(minimessage));
    }

    default void broadcast(Predicate<CommandSender> filter, String minimessage) {
        RSLib.getInstance().getAdventure().filter(filter).sendMessage(ComponentFormatter.mini(minimessage));
    }

    default void send(CommandSender sender, Component component) {
        Audience audience = RSLib.getInstance().getAdventure().sender(sender);
        audience.sendMessage(component);
    }

    default void send(Player player, Component component) {
        Audience audience = RSLib.getInstance().getAdventure().player(player);
        audience.sendMessage(component);
    }

    default void send(Audience audience, Component component) {
        audience.sendMessage(component);
    }

    default void broadcast(Component component) {
        RSLib.getInstance().getAdventure().all().sendMessage(component);
    }

    default void broadcast(Predicate<CommandSender> filter, Component component) {
        RSLib.getInstance().getAdventure().filter(filter).sendMessage(component);
    }

}
