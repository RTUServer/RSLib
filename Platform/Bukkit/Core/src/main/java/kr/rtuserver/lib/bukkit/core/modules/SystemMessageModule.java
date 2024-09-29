package kr.rtuserver.lib.bukkit.core.modules;

import kr.rtuserver.lib.bukkit.api.RSPlugin;
import kr.rtuserver.lib.bukkit.api.config.RSConfiguration;
import lombok.Getter;

@Getter
public class SystemMessageModule extends RSConfiguration implements kr.rtuserver.lib.bukkit.api.core.modules.SystemMessageModule {
    private String lore = "<gradient:#3df559:#3db8f5>시스템 메세지</gradient>\n<gray>%servertime_yyyy-MM-dd a h:mm%</gray>";

    public SystemMessageModule(RSPlugin plugin) {
        super(plugin, "Modules", "SystemMessage.yml", 1);
        setup(this);
    }

    private void init() {
        lore = getString("lore", lore, """
                시스템 메세제의 호버 메세지입니다""");
    }
}
