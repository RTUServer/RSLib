package com.github.ipecter.rtuserver.lib.bukkit.internal.listeners;

import com.github.ipecter.rtuserver.lib.bukkit.RSLib;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.listener.RSListener;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.util.format.ComponentFormatter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;

public class JoinListener extends RSListener {

    private final RSLib lib;

    public JoinListener(RSLib plugin) {
        super(plugin);
        this.lib = plugin;
    }

    @EventHandler
    public void motd(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (!player.isOp()) return;
        Map<String, RSPlugin> plugins = lib.getPlugins();
        Audience audience = lib.getAdventure().player(player);
        for (RSPlugin plugin : plugins.values()) {
            if (!plugin.getConfigurations().getSetting().isMotd()) continue;
            Component component = plugin.getPrefix().append(ComponentFormatter.mini(
                    plugin.getName() + " developed by " + String.join(" & ", plugin.getDescription().getAuthors())));
            audience.sendMessage(component);
        }
    }
}
