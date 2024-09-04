package com.github.ipecter.rtuserver.lib.plugin;

import com.github.ipecter.rtuserver.lib.bukkit.api.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.api.util.platform.MinecraftVersion;
import com.github.ipecter.rtuserver.lib.bukkit.api.util.platform.SystemEnviroment;
import com.github.ipecter.rtuserver.lib.core.RSFramework;
import com.github.ipecter.rtuserver.lib.plugin.commands.RSLibCommand;
import com.github.ipecter.rtuserver.lib.plugin.modules.Modules;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.impl.PacketCallback;
import org.bukkit.Bukkit;
import org.bukkit.permissions.PermissionDefault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RSLib extends RSPlugin {

    @Getter
    private static RSLib instance;
    @Getter
    private final PacketCallback callable = new PacketCallback(this::onPacket);
    @Getter
    private Modules modules;
    @Getter
    private Dependencies dependencies;

    @Autowired
    private ApplicationContext ctx;

    @Override
    protected void load() {
        instance = this;
        ctx.getBean(RSFramework.class).load(this);
    }

    @Override
    protected void onPacket(ProtoConnection connection, Object object) {
    }

    @Override
    protected void enable() {
        ctx.getBean(RSFramework.class).enable(this);
        printStartUp();

        modules = new Modules(this);
        dependencies = new Dependencies(this);

        registerPermission(getName() + ".motd", PermissionDefault.OP);
        registerCommand(new RSLibCommand(this));
    }

    private void printStartUp() {
        String str = """
                RSLib | Version: %version% | Bukkit: %bukkit% | NMS: %nms% | OS: %os% | JDK: %jdk%
                ╔ Developed by ════════════════════════════════════════════════════════════════════════════╗
                ║ ░█▀▄░█░█░▀█▀░█▀█░█▀▀░█▀▄░░░▀█▀░█▀▀░█▀▀░█░█░█▀█░█▀█░█░░░█▀█░█▀▀░█░█░░░█░█░█▀█░▀█▀░█▀▀░█░█ ║
                ║ ░█▀▄░█░█░░█░░█░█░█▀▀░█░█░░░░█░░█▀▀░█░░░█▀█░█░█░█░█░█░░░█░█░█░█░░█░░░░█░█░█░█░░█░░█▀▀░░█░ ║
                ║ ░▀░▀░▀▀▀░▀▀▀░▀░▀░▀▀▀░▀▀░░░░░▀░░▀▀▀░▀▀▀░▀░▀░▀░▀░▀▀▀░▀▀▀░▀▀▀░▀▀▀░░▀░░░░▀▀▀░▀░▀░▀▀▀░▀░░░░▀░ ║
                ╚══════════════════════════════════════════════════════════════════════════════════════════╝
                """
                .replace("%version%", getDescription().getVersion())
                .replace("%bukkit%", Bukkit.getName() + "-" + MinecraftVersion.getAsText())
                //.replace("%nms%", nmsVersion)
                .replace("%os%", SystemEnviroment.getOS())
                .replace("%jdk%", SystemEnviroment.getJDKVersion());
        System.out.println(str);
    }
}
