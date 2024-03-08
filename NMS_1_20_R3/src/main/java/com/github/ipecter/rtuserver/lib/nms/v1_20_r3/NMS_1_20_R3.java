package com.github.ipecter.rtuserver.lib.nms.v1_20_r3;

import com.github.ipecter.rtuserver.lib.nms.NMSBiome;
import com.github.ipecter.rtuserver.lib.nms.NMS;

public class NMS_1_20_R3 implements NMS {

    private final NMSBiome biome = new Biome();

    @Override
    public NMSBiome biome() {
        return biome;
    }
}
