package com.github.ipecter.rtuserver.lib.plugin.modules;

import com.github.ipecter.rtuserver.lib.plugin.RSLib;
import lombok.Getter;

@Getter
public class Modules {

    private final CommandModule commandModule;
    private final SystemMessageModule systemMessageModule;

    public Modules(RSLib plugin) {
        commandModule = new CommandModule(plugin);
        systemMessageModule = new SystemMessageModule(plugin);
    }

    public void reload() {
        commandModule.reload();
        systemMessageModule.reload();
    }
}
