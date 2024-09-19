package com.github.ipecter.rtuserver.lib.bukkit.plugin.injector;

import com.github.ipecter.rtuserver.lib.bukkit.api.core.RSFramework;
import com.google.inject.AbstractModule;

public class InjectorModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(RSFramework.class).to(com.github.ipecter.rtuserver.lib.bukkit.core.RSFramework.class);
    }
}
