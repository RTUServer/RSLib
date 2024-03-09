package com.github.ipecter.rtuserver.lib.nms.v1_19_r1;

import com.github.ipecter.rtuserver.lib.nms.NMS;
import com.github.ipecter.rtuserver.lib.nms.NMSBiome;

public class NMS_1_19_R1 implements NMS {

    private final NMSBiome biome = new Biome();

    @Override
    public NMSBiome biome() {
        return biome;
    }
}
