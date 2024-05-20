package com.github.ipecter.rtuserver.lib.modules;

import com.github.ipecter.rtuserver.lib.RSLib;
import com.github.ipecter.rtuserver.lib.plugin.config.RSConfiguration;
import lombok.Getter;

@Getter
public class CommandModule extends RSConfiguration {
    private int cooldown = 30;

    public CommandModule() {
        super(RSLib.getInstance(), "Modules", "Command.yml", 1);
        setup(this);
    }

    private void init() {
        cooldown = getInt("cooldown", cooldown, """
                RS 플러그인의 명령어 재사용 대기 시간 (틱)""");
    }
}
