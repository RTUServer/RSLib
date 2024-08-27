package com.github.ipecter.rtuserver.lib.bukkit.plugin.listener;

import com.github.ipecter.rtuserver.lib.bukkit.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.shortcut.RSAbstract;
import org.bukkit.event.Listener;


public abstract class RSListener extends RSAbstract implements Listener {

    public RSListener(RSPlugin plugin) {
        super(plugin);
    }
}
