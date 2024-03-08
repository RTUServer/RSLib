package com.github.ipecter.rtuserver.lib.util.common;

import com.github.ipecter.rtuserver.lib.RSLib;
import com.github.ipecter.rtuserver.lib.managers.config.SystemMessageConfig;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
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

    public static Component systemMessage(Component prefix, String minimessage) {
        return systemMessage((CommandSender) null, prefix, formatted(minimessage));
    }

    public static Component systemMessage(Component prefix, Component component) {
        return systemMessage((CommandSender) null, prefix, component);
    }

    public static Component systemMessage(CommandSender sender, Component prefix, String minimessage) {
        return systemMessage(sender, prefix, formatted(minimessage));
    }

    public static Component systemMessage(CommandSender sender, Component prefix, Component component) {
        SystemMessageConfig config = RSLib.getInstance().getConfigManager().getSystemMessage();
        Component lore = formatted(sender, config.getLore());
        return prefix.append(component).hoverEvent(HoverEvent.showText(lore));
    }

    public static Component systemMessage(String minimessage) {
        return systemMessage((CommandSender) null, formatted(minimessage));
    }


    public static Component systemMessage(Component component) {
        return systemMessage((CommandSender) null, component);
    }

    public static Component systemMessage(CommandSender sender, String minimessage) {
        return systemMessage(sender, formatted(minimessage));
    }

    public static Component systemMessage(CommandSender sender, Component component) {
        SystemMessageConfig config = RSLib.getInstance().getConfigManager().getSystemMessage();
        Component prefix = formatted(sender, config.getPrefix());
        Component lore = formatted(sender, config.getLore());
        return prefix.append(component).hoverEvent(HoverEvent.showText(lore));
    }

}
