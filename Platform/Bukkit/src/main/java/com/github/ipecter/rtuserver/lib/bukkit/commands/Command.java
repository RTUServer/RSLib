package com.github.ipecter.rtuserver.lib.bukkit.commands;

import com.github.ipecter.rtuserver.lib.bukkit.RSLib;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.command.CommandData;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.command.RSCommand;

import java.util.List;

public class Command extends RSCommand {

    private final RSLib lib = RSLib.getInstance();

    public Command(RSPlugin plugin) {
        super(plugin, "rslib", true);
    }

    @Override
    public boolean command(CommandData data) {
        return false;
    }

    @Override
    public void reload(CommandData command) {
        lib.getModules().reload();
    }

    @Override
    public List<String> tabComplete(CommandData data) {
        return List.of();
    }
}
