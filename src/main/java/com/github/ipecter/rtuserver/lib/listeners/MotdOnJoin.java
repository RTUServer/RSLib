package com.github.ipecter.rtuserver.lib.listeners;

import com.github.ipecter.rtuserver.lib.RSLib;
import com.github.ipecter.rtuserver.lib.plugin.RSListener;
import com.github.ipecter.rtuserver.lib.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.util.common.ComponentUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.Map;

public class MotdOnJoin extends RSListener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!RSLib.getInstance().getConfigManager().getSetting().isMotd()) return;
        Map<String, RSPlugin> plugins = RSLib.getInstance().getPlugins();
        Audience audience = this.getPlugin().getAdventure().player(e.getPlayer());
        for (String key : plugins.keySet()) {
            RSPlugin plugin = plugins.get(key);
            audience.sendMessage(plugin.getPrefix()
                    .append(ComponentUtil.miniMessage(
                            plugin.getName() + " developed by " + String.join(" & ", plugin.getDescription().getAuthors())
                    )));
        }
    }
}
