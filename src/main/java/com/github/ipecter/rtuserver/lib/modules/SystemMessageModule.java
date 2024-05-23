package com.github.ipecter.rtuserver.lib.modules;

import com.github.ipecter.rtuserver.lib.RSLib;
import com.github.ipecter.rtuserver.lib.plugin.config.RSConfiguration;
import lombok.Getter;

@Getter
public class SystemMessageModule extends RSConfiguration {
    private String lore = "<gradient:#3df559:#3db8f5>시스템 메세지</gradient>\n<gray>%servertime_yyyy-MM-dd a h:mm%</gray>";

    public SystemMessageModule() {
        super(RSLib.getInstance(), "Modules", "SystemMessage.yml", 1);
        setup(this);
    }

    private void init() {
        lore = getString("lore", lore, """
                시스템 메세제의 호버 메세지입니다""");
    }
}
