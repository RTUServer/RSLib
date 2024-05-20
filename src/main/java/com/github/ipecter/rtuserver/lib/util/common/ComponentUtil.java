package com.github.ipecter.rtuserver.lib.util.common;

import com.github.ipecter.rtuserver.lib.RSLib;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ComponentUtil {

    public static Component miniMessage(String miniMessage) {
        return MiniMessage.miniMessage().deserialize(miniMessage);
    }

    public static Component formatted(String msg) {
        return formatted(null, msg);
    }

    public static Component formatted(CommandSender sender, String miniMessage) {
        return miniMessage(RSLib.getInstance().isEnabledDependency("PlaceholderAPI") ? PlaceholderAPI.setPlaceholders((sender instanceof Player player) ? player : null, miniMessage) : miniMessage);
    }

    public static String toString(Component component) {
        return toString(LegacyComponentSerializer.SECTION_CHAR, component);
    }

    public static String toString(char legacyCharacter, Component component) {
        return LegacyComponentSerializer.legacy(legacyCharacter).serialize(component);
    }
}
