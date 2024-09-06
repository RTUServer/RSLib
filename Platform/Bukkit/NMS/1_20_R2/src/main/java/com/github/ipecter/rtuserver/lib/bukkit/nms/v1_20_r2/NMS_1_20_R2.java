package com.github.ipecter.rtuserver.lib.bukkit.nms.v1_20_r2;

import com.github.ipecter.rtuserver.lib.bukkit.api.nms.NMS;
import com.github.ipecter.rtuserver.lib.bukkit.api.nms.NMSBiome;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.v1_20_R2.CraftServer;

public class NMS_1_20_R2 implements NMS {

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
