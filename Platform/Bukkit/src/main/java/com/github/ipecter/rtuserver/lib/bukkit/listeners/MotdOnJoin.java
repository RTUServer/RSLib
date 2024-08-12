package com.github.ipecter.rtuserver.lib.bukkit.listeners;

import com.github.ipecter.rtuserver.lib.bukkit.RSLib;
import com.github.ipecter.rtuserver.lib.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.plugin.listener.RSListener;
import com.github.ipecter.rtuserver.lib.bukkit.util.common.ComponentUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;

public class MotdOnJoin extends RSListener {

    private final RSLib lib;

    public MotdOnJoin(RSLib plugin) {
        super(plugin);
        this.lib = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (!player.isOp()) return;
        Map<String, RSPlugin> plugins = lib.getPlugins();
        Audience audience = lib.getAdventure().player(player);
        for (RSPlugin plugin : plugins.values()) {
            if (!plugin.getConfigurations().getSetting().isMotd()) continue;
            Component component = plugin.getPrefix().append(ComponentUtil.miniMessage(
                    plugin.getName() + " developed by " + String.join(" & ", plugin.getDescription().getAuthors())));
            audience.sendMessage(component);
        }
    }
}
