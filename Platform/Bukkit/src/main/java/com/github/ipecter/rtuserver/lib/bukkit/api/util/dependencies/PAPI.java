package com.github.ipecter.rtuserver.lib.bukkit.api.util.dependencies;

import com.github.ipecter.rtuserver.lib.bukkit.RSLib;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PAPI {

    public static String parse(CommandSender sender, String message) {
        if (!RSLib.getInstance().isEnabledDependency("PlaceholderAPI")) return message;
        return PlaceholderAPI.setPlaceholders((sender instanceof Player player) ? player : null, message);
    }

    public static @NotNull TagResolver tag(final @NotNull Player player) {
        return TagResolver.resolver("papi", (argumentQueue, context) -> {
            final String papiPlaceholder = argumentQueue.popOr("papi tag requires an argument").value();
            if (!RSLib.getInstance().isEnabledDependency("PlaceholderAPI"))
                return Tag.selfClosingInserting(Component.empty());
            final String parsedPlaceholder = PlaceholderAPI.setPlaceholders(player, '%' + papiPlaceholder + '%');
            final Component componentPlaceholder = LegacyComponentSerializer.legacySection().deserialize(parsedPlaceholder);
            return Tag.selfClosingInserting(componentPlaceholder);
        });
    }
}
