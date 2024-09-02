package com.github.ipecter.rtuserver.lib.bukkit.api.listener;

import com.github.ipecter.rtuserver.lib.bukkit.api.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.api.config.CommandConfiguration;
import com.github.ipecter.rtuserver.lib.bukkit.api.config.MessageConfiguration;
import com.github.ipecter.rtuserver.lib.bukkit.api.config.SettingConfiguration;
import com.github.ipecter.rtuserver.lib.bukkit.api.shortcut.Message;
import com.github.ipecter.rtuserver.lib.bukkit.api.shortcut.Scheduler;
import lombok.Getter;
import org.bukkit.event.Listener;


@Getter
public abstract class RSListener implements Listener, Message, Scheduler {

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
