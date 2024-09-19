package com.github.ipecter.rtuserver.lib.bukkit.api.util.format;

import com.github.ipecter.rtuserver.lib.bukkit.api.core.RSFramework;
import com.google.inject.Inject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ComponentFormatter {

    @Inject
    private static RSFramework framework;

    public static Component mini(String miniMessage) {
        return MiniMessage.miniMessage().deserialize(miniMessage);
    }

    public static Component parse(String msg) {
        return parse(null, msg);
    }

    public static Component parse(CommandSender sender, String miniMessage) {
        return mini(framework.isEnabledDependency("PlaceholderAPI") ? PlaceholderAPI.setPlaceholders((sender instanceof Player player) ? player : null, miniMessage) : miniMessage);
    }

    public static String legacy(Component component) {
        return legacy(LegacyComponentSerializer.SECTION_CHAR, component);
    }

    public static String legacy(char legacyCharacter, Component component) {
        return LegacyComponentSerializer.legacy(legacyCharacter).serialize(component);
    }

//    public static Component system(CommandSender sender, String miniMessage) {
//        Component lore = parse(sender, framework.getModules().getSystemMessageModule().getLore());
//        return parse(miniMessage).hoverEvent(HoverEvent.showText(lore));
//    }
//
//    public static Component system(CommandSender sender, Component component) {
//        Component lore = parse(sender, framework.getModules().getSystemMessageModule().getLore());
//        return component.hoverEvent(HoverEvent.showText(lore));
//    }


}
