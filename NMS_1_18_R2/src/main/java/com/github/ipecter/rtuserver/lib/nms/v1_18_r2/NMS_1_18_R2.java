package com.github.ipecter.rtuserver.lib.nms.v1_18_r2;

import com.github.ipecter.rtuserver.lib.nms.NMSBiome;
import com.github.ipecter.rtuserver.lib.nms.NMS;

public class NMS_1_18_R2 implements NMS {

    private final NMSBiome biome = new Biome();

    @Override
    public NMSBiome biome() {
        return biome;
    }
}
