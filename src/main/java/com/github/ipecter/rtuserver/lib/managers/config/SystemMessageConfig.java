package com.github.ipecter.rtuserver.lib.managers.config;

import lombok.Data;

@Data
public class SystemMessageConfig {

    private String prefix = "<gradient:#00f260:#057eff>Festival » </gradient>";
    private String lore = "<gradient:#3df559:#3db8f5>시스템 메세지</gradient>\n<gray>%servertime_yyyy-MM-dd a h:mm%</gray>";

}
