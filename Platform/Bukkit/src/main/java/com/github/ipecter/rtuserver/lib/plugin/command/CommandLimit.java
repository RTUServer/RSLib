package com.github.ipecter.rtuserver.lib.plugin.command;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import com.github.ipecter.rtuserver.lib.bukkit.RSLib;
import com.github.ipecter.rtuserver.lib.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.plugin.listener.RSListener;
import com.github.ipecter.rtuserver.lib.bukkit.util.common.VersionUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CommandLimit implements Runnable {

    @Getter
    private final Map<UUID, Integer> executeLimit = new ConcurrentHashMap<>();
    @Getter
    private final Map<UUID, Integer> tabCompleteLimit = new ConcurrentHashMap<>();

    private final int limit;

    public CommandLimit(RSLib plugin) {
        limit = plugin.getModules().getCommandModule().getTabCompleteLimit();
        if (VersionUtil.isPaper()) plugin.registerEvent(new Paper(plugin));
        else plugin.registerEvent(new Spigot(plugin));
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, 0, 1);
    }

    @Override
    public void run() {
        for (UUID uuid : executeLimit.keySet()) {
            if (executeLimit.get(uuid) > 0) executeLimit.put(uuid, executeLimit.get(uuid) - 1);
            else executeLimit.remove(uuid);
        }
        for (UUID uuid : tabCompleteLimit.keySet()) {
            if (tabCompleteLimit.get(uuid) > 0) tabCompleteLimit.put(uuid, tabCompleteLimit.get(uuid) - 1);
            else tabCompleteLimit.remove(uuid);
        }
    }

    class Spigot extends RSListener {
        public Spigot(RSPlugin plugin) {
            super(plugin);
        }

        @EventHandler
        public void onTabComplete(TabCompleteEvent e) {
            if (e.getSender() instanceof Player player) {
                UUID uuid = player.getUniqueId();
                if (tabCompleteLimit.getOrDefault(player.getUniqueId(), 0) <= 0)
                    tabCompleteLimit.put(player.getUniqueId(), limit);
                else e.setCancelled(true);
            }
        }
    }

    class Paper extends RSListener {
        public Paper(RSPlugin plugin) {
            super(plugin);
        }

        @EventHandler
        public void onTabComplete(AsyncTabCompleteEvent e) {
            if (e.getSender() instanceof Player player) {
                UUID uuid = player.getUniqueId();
                if (tabCompleteLimit.getOrDefault(player.getUniqueId(), 0) <= 0)
                    tabCompleteLimit.put(player.getUniqueId(), limit);
                else e.setCancelled(true);
            }
        }
    }
}
