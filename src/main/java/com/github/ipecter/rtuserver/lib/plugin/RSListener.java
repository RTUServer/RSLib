package com.github.ipecter.rtuserver.lib.plugin;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public abstract class RSListener implements Listener {
    @Getter
    private final RSPlugin plugin = RSPlugin.getPlugin();

    public RSListener() {
        this(true);
    }

    public RSListener(boolean isEnable) {
        if (isEnable) Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}
