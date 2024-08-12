package com.github.ipecter.rtuserver.lib.plugin.listener;

import com.github.ipecter.rtuserver.lib.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.plugin.abstracts.RSAbstract;
import com.github.ipecter.rtuserver.lib.plugin.config.CommandConfiguration;
import com.github.ipecter.rtuserver.lib.plugin.config.MessageConfiguration;
import com.github.ipecter.rtuserver.lib.plugin.config.SettingConfiguration;
import lombok.Data;
import org.bukkit.event.Listener;

@Data
public abstract class RSListener extends RSAbstract implements Listener {

    private final RSPlugin plugin;
    private final SettingConfiguration setting;
    private final MessageConfiguration message;
    private final CommandConfiguration command;

    public RSListener(RSPlugin plugin) {
        this.plugin = plugin;
        this.setting = plugin.getConfigurations().getSetting();
        this.message = plugin.getConfigurations().getMessage();
        this.command = plugin.getConfigurations().getCommand();
    }
}
