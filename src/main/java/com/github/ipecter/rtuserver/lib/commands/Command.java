package com.github.ipecter.rtuserver.lib.commands;

import com.github.ipecter.rtuserver.lib.RSLib;
import com.github.ipecter.rtuserver.lib.nms.v1_20_r3.Biome;
import com.github.ipecter.rtuserver.lib.plugin.command.CommandData;
import com.github.ipecter.rtuserver.lib.plugin.command.CommandType;
import com.github.ipecter.rtuserver.lib.plugin.command.RSCommand;
import org.bukkit.entity.Player;

import java.util.List;

public class Command extends RSCommand {

    private final RSLib lib = RSLib.getInstance();

    public Command() {
        super(RSLib.getInstance(), "rslib", CommandType.MAIN);
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
