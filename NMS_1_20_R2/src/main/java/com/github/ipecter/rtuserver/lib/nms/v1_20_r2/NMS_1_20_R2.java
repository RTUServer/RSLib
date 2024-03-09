package com.github.ipecter.rtuserver.lib.nms.v1_20_r2;

import com.github.ipecter.rtuserver.lib.nms.NMS;
import com.github.ipecter.rtuserver.lib.nms.NMSBiome;

public class NMS_1_20_R2 implements NMS {

    private final NMSBiome biome = new Biome();

    @Override
    public NMSBiome biome() {
        return biome;
    }
}
