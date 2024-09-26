package com.github.ipecter.rtuserver.lib.bukkit.plugin.injector;

import com.github.ipecter.rtuserver.lib.bukkit.api.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.api.core.RSFramework;
import com.github.ipecter.rtuserver.lib.bukkit.api.utility.compatible.BlockCompat;
import com.github.ipecter.rtuserver.lib.bukkit.api.utility.compatible.ItemCompat;
import com.github.ipecter.rtuserver.lib.bukkit.api.utility.dependencies.PAPI;
import com.github.ipecter.rtuserver.lib.bukkit.api.utility.format.ComponentFormatter;
import com.github.ipecter.rtuserver.lib.bukkit.api.utility.format.TextFomatter;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class InjectorModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(RSFramework.class)
                .to(com.github.ipecter.rtuserver.lib.bukkit.core.RSFramework.class)
                .in(Scopes.SINGLETON);
        requestStaticInjection(
                BlockCompat.class,
                ItemCompat.class,
                PAPI.class,
                ComponentFormatter.class,
                TextFomatter.class
        );
    }
}
