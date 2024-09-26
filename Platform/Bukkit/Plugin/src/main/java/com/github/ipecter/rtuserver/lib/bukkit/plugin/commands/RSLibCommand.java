package com.github.ipecter.rtuserver.lib.bukkit.plugin.commands;

import com.github.ipecter.rtuserver.lib.bukkit.api.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.api.command.RSCommand;
import com.github.ipecter.rtuserver.lib.bukkit.api.command.RSCommandData;
import com.github.ipecter.rtuserver.lib.bukkit.api.core.RSFramework;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.RSLib;
import com.google.inject.Inject;

import java.util.List;

public class RSLibCommand extends RSCommand {

    private final RSLib lib = RSLib.getInstance();
    @Inject
    private RSFramework framework;

    public RSLibCommand(RSPlugin plugin) {
        super(plugin, "rslib", true);

    }

    @Override
    public boolean execute(RSCommandData data) {
        System.out.println(framework.getNMSVersion());
        return false;
    }

    @Override
    public void reload(RSCommandData data) {
        getFramework().getModules().reload();
    }

    @Override
    public List<String> tabComplete(RSCommandData data) {
        return List.of();
    }
}
