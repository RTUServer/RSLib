package com.github.ipecter.rtuserver.lib.modules;

import lombok.Getter;

@Getter
public class Modules {

    private final CommandModule commandModule = new CommandModule();

    public void reload() {
        commandModule.reload();
    }
}
