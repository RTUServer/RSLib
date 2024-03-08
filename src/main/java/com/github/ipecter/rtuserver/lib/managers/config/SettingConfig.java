package com.github.ipecter.rtuserver.lib.managers.config;

import lombok.Data;

@Data
public class SettingConfig {

    private boolean enablePlugin = true;
    private boolean motd = true;
    private String locale = "EN";

}
