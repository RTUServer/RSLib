package com.github.ipecter.rtuserver.lib.bukkit.plugin;

import com.github.ipecter.rtuserver.lib.bukkit.api.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.api.core.RSFramework;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.commands.RSLibCommand;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.injector.InjectorModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import lombok.Getter;
import me.mrnavastar.protoweaver.api.callback.PacketCallback;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import org.bukkit.Bukkit;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;

public class RSLib extends RSPlugin {

    @Getter
    private static RSLib instance;
    @Getter
    private final PacketCallback callable = new PacketCallback(this::onPacket);
    @Inject
    private RSFramework framework;

    @Override
    protected void load() {
        instance = this;
        Injector injector = Guice.createInjector(new InjectorModule());
        injector.injectMembers(this);
        System.out.println("A");
//        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
//            plugin.
//        }
        Reflections reflections = new Reflections("com",
        new FieldAnnotationsScanner());
        reflections.getFieldsAnnotatedWith(Inject.class).forEach(field -> {
            System.out.println(field.getDeclaringClass().getName());
            //injector.injectMembers(field.getDeclaringClass());
        });
        System.out.println("B");
        framework.load(this);
    }

    @Override
    protected void onPacket(ProtoConnection connection, Object object) {
    }

    @Override
    protected void enable() {
        framework.enable(this);

        registerPermission(getName() + ".motd", PermissionDefault.OP);
        registerCommand(new RSLibCommand(this));
    }

    @Override
    protected void disable() {
        framework.disable(this);
    }
}
