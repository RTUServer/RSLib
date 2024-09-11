package com.github.ipecter.rtuserver.lib.plugin;

import com.github.ipecter.rtuserver.lib.bukkit.api.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.api.core.RSFramework;
import com.github.ipecter.rtuserver.lib.bukkit.api.util.platform.MinecraftVersion;
import com.github.ipecter.rtuserver.lib.bukkit.api.util.platform.SystemEnviroment;
import com.github.ipecter.rtuserver.lib.plugin.commands.RSLibCommand;
import com.github.ipecter.rtuserver.lib.plugin.injector.InjectorModule;
import com.github.ipecter.rtuserver.lib.bukkit.core.modules.Modules;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.callback.PacketCallback;
import org.bukkit.Bukkit;
import org.bukkit.permissions.PermissionDefault;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RSLib extends RSPlugin {

    @Getter
    private static RSLib instance;
    @Getter
    private final PacketCallback callable = new PacketCallback(this::onPacket);
    @Getter
    private Dependencies dependencies;
    @Inject
    private RSFramework framework;

    @Override
    protected void load() {
        instance = this;
        Injector injector = Guice.createInjector(new InjectorModule());
        injector.injectMembers(this);
        framework.load(this);
    }

    @Override
    protected void onPacket(ProtoConnection connection, Object object) {
    }

    @Override
    protected void enable() {
        framework.enable(this);
        dependencies = new Dependencies(this);

        registerPermission(getName() + ".motd", PermissionDefault.OP);
        registerCommand(new RSLibCommand(this));
    }

    @Override
    protected void disable() {
        framework.disable(this);
    }
}
