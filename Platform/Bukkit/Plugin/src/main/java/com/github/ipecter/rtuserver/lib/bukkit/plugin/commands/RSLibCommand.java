package com.github.ipecter.rtuserver.lib.bukkit.plugin.commands;

import com.github.ipecter.rtuserver.lib.bukkit.api.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.api.command.RSCommand;
import com.github.ipecter.rtuserver.lib.bukkit.api.command.RSCommandData;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.RSLib;

import java.util.List;

public class RSLibCommand extends RSCommand {

    private final RSLib lib = RSLib.getInstance();

    public RSLibCommand(RSPlugin plugin) {
        super(plugin, "rslib", true);

    }

    @Override
    public boolean execute(RSCommandData data) {
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
