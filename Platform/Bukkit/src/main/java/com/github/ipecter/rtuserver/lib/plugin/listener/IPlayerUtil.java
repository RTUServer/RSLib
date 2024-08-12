package com.github.ipecter.rtuserver.lib.plugin.listener;

import com.github.ipecter.rtuserver.lib.bukkit.RSLib;
import com.github.ipecter.rtuserver.lib.bukkit.util.common.ComponentUtil;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;

public interface IPlayerUtil {

    default void sendMessage(Player player, String message) {
        Audience audience = RSLib.getInstance().getAdventure().player(player);
        audience.sendMessage(ComponentUtil.miniMessage(message));
    }
}
