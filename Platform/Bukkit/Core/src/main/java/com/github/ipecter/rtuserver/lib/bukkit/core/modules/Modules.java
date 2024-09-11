package com.github.ipecter.rtuserver.lib.bukkit.core.modules;

import com.github.ipecter.rtuserver.lib.bukkit.api.RSPlugin;
import lombok.Getter;

@Getter
public class Modules implements com.github.ipecter.rtuserver.lib.bukkit.api.core.modules.Modules {

    private final CommandModule commandModule;
    private final SystemMessageModule systemMessageModule;

    public Modules(RSPlugin plugin) {
        commandModule = new CommandModule(plugin);
        systemMessageModule = new SystemMessageModule(plugin);
    }

    public void reload() {
        commandModule.reload();
        systemMessageModule.reload();
    }
}
