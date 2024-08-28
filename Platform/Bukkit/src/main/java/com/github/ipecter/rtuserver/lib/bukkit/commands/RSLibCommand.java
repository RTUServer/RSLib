package com.github.ipecter.rtuserver.lib.bukkit.commands;

import com.github.ipecter.rtuserver.lib.bukkit.RSLib;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.command.CommandData;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.command.RSCommand;

import java.util.List;

public class RSLibCommand extends RSCommand {

    private final RSLib lib = RSLib.getInstance();

    public RSLibCommand(RSPlugin plugin) {
        super(plugin, "rslib", true);
    }

    @Override
    public boolean execute(CommandData data) {
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
