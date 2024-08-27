package com.github.ipecter.rtuserver.lib.bukkit.modules;

import com.github.ipecter.rtuserver.lib.bukkit.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.config.RSConfiguration;
import lombok.Getter;

@Getter
public class CommandModule extends RSConfiguration {
    private int executeLimit = 30;
    private int tabCompleteLimit = 10;

    public CommandModule(RSPlugin plugin) {
        super(plugin, "Modules", "Command.yml", 1);
        setup(this);
    }

    private void init() {
        executeLimit = getInt("execute.limit", executeLimit, """
                RS 플러그인의 명령어 재사용 대기 시간 (틱)""");
        tabCompleteLimit = getInt("tabComplete.limit", tabCompleteLimit, """
                RS 플러그인의 명령어 자동완성 재사용 대기 시간 (틱)""");
    }
}
