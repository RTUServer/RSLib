package com.github.ipecter.rtuserver.lib.plugin;

import lombok.Getter;
import org.bukkit.event.Listener;

public abstract class RSListener implements Listener {
    @Getter
    private final RSPlugin plugin = RSPlugin.getPlugin();
}
