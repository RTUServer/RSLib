package com.github.ipecter.rtuserver.lib.nms.v1_19_r2;

import com.github.ipecter.rtuserver.lib.nms.NMS;
import com.github.ipecter.rtuserver.lib.nms.NMSBiome;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.v1_19_R2.CraftServer;

public class NMS_1_19_R2 implements NMS {

    private final NMSBiome biome = new Biome();

    @Override
    public NMSBiome biome() {
        return biome;
    }

    @Override
    public CommandMap commandMap() {
        return ((CraftServer) Bukkit.getServer()).getCommandMap();
    }
}
