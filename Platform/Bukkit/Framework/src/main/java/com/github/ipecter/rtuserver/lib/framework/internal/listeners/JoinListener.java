package com.github.ipecter.rtuserver.lib.framework.internal.listeners;

import com.github.ipecter.rtuserver.lib.framework.RSFramework;
import com.github.ipecter.rtuserver.lib.bukkit.api.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.api.listener.RSListener;
import com.github.ipecter.rtuserver.lib.bukkit.api.util.format.ComponentFormatter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;

public class JoinListener extends RSListener {

    private final RSFramework lib;

    public JoinListener(RSFramework plugin) {
        super(plugin);
        this.lib = plugin;
    }

    @EventHandler
    public void motdMessage(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (!player.hasPermission(getPlugin().getName() + ".motd")) return;
        Map<String, RSPlugin> plugins = lib.getPlugins();
        Audience audience = lib.getAdventure().player(player);
        for (RSPlugin plugin : plugins.values()) {
            if (!plugin.getConfigurations().getSetting().isMotd()) continue;
            String str = "%s developed by %s".formatted(player.getName(), String.join(" & ", plugin.getDescription().getAuthors()));
            Component component = plugin.getPrefix().append(ComponentFormatter.mini(str));
            audience.sendMessage(component);
        }
    }
}
