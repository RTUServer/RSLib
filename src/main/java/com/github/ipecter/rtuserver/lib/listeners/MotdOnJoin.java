package com.github.ipecter.rtuserver.lib.listeners;

import com.github.ipecter.rtuserver.lib.RSLib;
import com.github.ipecter.rtuserver.lib.plugin.RSListener;
import com.github.ipecter.rtuserver.lib.plugin.RSPlugin;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

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
                    .append(MiniMessage.miniMessage().deserialize(
                            plugin.getName() + " developed by " + String.join(" & ", plugin.getDescription().getAuthors())
                    )));
        }
    }
}
