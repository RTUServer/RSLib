package io.papermc.paper.adventure;

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;

public final class DisplayNames {
    private DisplayNames() {
    }

    public static String getLegacy(final CraftPlayer player) {
        return getLegacy(player.getHandle());
    }

    public static String getLegacy(final ServerPlayer player) {
        final String legacy = player.displayName;
        if (legacy != null) {
            return PaperAdventure.LEGACY_SECTION_UXRC.serialize(player.adventure$displayName) + ChatColor.getLastColors(player.displayName);
        }
        return PaperAdventure.LEGACY_SECTION_UXRC.serialize(player.adventure$displayName);
    }
}
