package com.github.ipecter.rtuserver.lib.plugin.listener;

import com.github.ipecter.rtuserver.lib.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.plugin.abstracts.RSAbstract;
import com.github.ipecter.rtuserver.lib.plugin.config.CommandConfiguration;
import com.github.ipecter.rtuserver.lib.plugin.config.MessageConfiguration;
import com.github.ipecter.rtuserver.lib.plugin.config.SettingConfiguration;
import lombok.Data;
import org.bukkit.event.Listener;


public abstract class RSListener extends RSAbstract implements Listener {

    public RSListener(RSPlugin plugin) {
        super(plugin);
    }
}
