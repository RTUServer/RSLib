package com.github.ipecter.rtuserver.lib.bukkit.api.listener;

import com.github.ipecter.rtuserver.lib.bukkit.api.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.api.config.impl.CommandConfiguration;
import com.github.ipecter.rtuserver.lib.bukkit.api.config.impl.MessageConfiguration;
import com.github.ipecter.rtuserver.lib.bukkit.api.config.impl.SettingConfiguration;
import com.github.ipecter.rtuserver.lib.bukkit.api.shortcut.Message;
import lombok.Getter;
import org.bukkit.event.Listener;


@Getter
public abstract class RSListener implements Listener {

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
