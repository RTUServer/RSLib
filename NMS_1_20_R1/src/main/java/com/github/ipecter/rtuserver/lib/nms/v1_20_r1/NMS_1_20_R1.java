package com.github.ipecter.rtuserver.lib.nms.v1_20_r1;

import com.github.ipecter.rtuserver.lib.nms.NMSBiome;
import com.github.ipecter.rtuserver.lib.nms.NMS;

public class NMS_1_20_R1 implements NMS {

    private final NMSBiome biome = new Biome();

    @Override
    public NMSBiome biome() {
        return biome;
    }
}
