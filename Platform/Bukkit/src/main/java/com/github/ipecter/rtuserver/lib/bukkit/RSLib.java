package com.github.ipecter.rtuserver.lib.bukkit;

import com.github.ipecter.rtuserver.lib.bukkit.commands.Command;
import com.github.ipecter.rtuserver.lib.bukkit.internal.listeners.InventoryListener;
import com.github.ipecter.rtuserver.lib.bukkit.internal.listeners.JoinListener;
import com.github.ipecter.rtuserver.lib.bukkit.internal.runnable.CommandLimit;
import com.github.ipecter.rtuserver.lib.bukkit.modules.Modules;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.util.platform.SystemEnviroment;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.util.platform.MinecraftVersion;
import com.github.ipecter.rtuserver.lib.nms.v1_17_r1.NMS_1_17_R1;
import com.github.ipecter.rtuserver.lib.nms.v1_18_r1.NMS_1_18_R1;
import com.github.ipecter.rtuserver.lib.nms.v1_18_r2.NMS_1_18_R2;
import com.github.ipecter.rtuserver.lib.nms.v1_19_r1.NMS_1_19_R1;
import com.github.ipecter.rtuserver.lib.nms.v1_19_r2.NMS_1_19_R2;
import com.github.ipecter.rtuserver.lib.nms.v1_19_r3.NMS_1_19_R3;
import com.github.ipecter.rtuserver.lib.nms.v1_20_r1.NMS_1_20_R1;
import com.github.ipecter.rtuserver.lib.nms.v1_20_r2.NMS_1_20_R2;
import com.github.ipecter.rtuserver.lib.nms.v1_20_r3.NMS_1_20_R3;
import com.github.ipecter.rtuserver.lib.nms.v1_20_r4.NMS_1_20_R4;
import com.github.ipecter.rtuserver.lib.nms.v1_21_r1.NMS_1_21_R1;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.impl.PacketCallback;
import me.mrnavastar.protoweaver.impl.bukkit.BukkitProtoWeaver;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RSLib extends RSPlugin {

    @Getter
    private static RSLib instance;
    @Getter
    private final Map<String, RSPlugin> plugins = new HashMap<>();
    private final Map<String, Boolean> hooks = new HashMap<>();
    private final PacketCallback callable = new PacketCallback(this::onPacket);
    @Getter
    private com.github.ipecter.rtuserver.lib.nms.NMS NMS;
    @Getter
    private BukkitProtoWeaver protoWeaver;
    private String nmsVersion;
    @Getter
    private Modules modules;
    @Getter
    private CommandLimit commandLimit;
    @Getter
    private Dependencies dependencies;

    public void loadPlugin(RSPlugin plugin) {
        console("<white>loading RSPlugin: " + plugin.getName() + "</white>");
        plugins.put(plugin.getName(), plugin);
    }

    public void unloadPlugin(RSPlugin plugin) {
        console("<white>unloading RSPlugin: " + plugin.getName() + "</white>");
        plugins.remove(plugin.getName());
    }

    public boolean isEnabledDependency(String dependencyName) {
        return hooks.getOrDefault(dependencyName, false);
    }

    public void hookDependency(String dependencyName) {
        hooks.put(dependencyName, Bukkit.getPluginManager().isPluginEnabled(dependencyName));
    }

    @Override
    protected void load() {
        instance = this;
        loadNMS();
    }

    private void loadNMS() {
        nmsVersion = MinecraftVersion.getNMS(MinecraftVersion.getAsText());
        switch (nmsVersion) {
            case "v1_17_R1" -> NMS = new NMS_1_17_R1();
            case "v1_18_R1" -> NMS = new NMS_1_18_R1();
            case "v1_18_R2" -> NMS = new NMS_1_18_R2();
            case "v1_19_R1" -> NMS = new NMS_1_19_R1();
            case "v1_19_R2" -> NMS = new NMS_1_19_R2();
            case "v1_19_R3" -> NMS = new NMS_1_19_R3();
            case "v1_20_R1" -> NMS = new NMS_1_20_R1();
            case "v1_20_R2" -> NMS = new NMS_1_20_R2();
            case "v1_20_R3" -> NMS = new NMS_1_20_R3();
            case "v1_20_R4" -> NMS = new NMS_1_20_R4();
            case "v1_21_R1" -> NMS = new NMS_1_21_R1();
            default -> {
                Bukkit.getLogger().warning("Server version is unsupported version, Disabling RSLib...");
                this.getServer().getPluginManager().disablePlugin(this);
            }
        }
        protoWeaver = new BukkitProtoWeaver(callable, getDataFolder().getPath(), nmsVersion);
    }

    @Override
    protected void onPacket(ProtoConnection connection, Object object) {
    }

    @Override
    protected void enable() {
        printStartUp();

        modules = new Modules(this);
        dependencies = new Dependencies(this);

        registerInternal();

        registerCommand(new Command(this));
    }

    private void registerInternal() {
        registerInternalRunnable();
        registerInternalListener();
    }

    private void registerInternalRunnable() {
        commandLimit = new CommandLimit(this);
    }

    private void registerInternalListener() {
        registerEvent(new JoinListener(this));
        registerEvent(new InventoryListener(this));
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
                .replace("%bukkit%",  Bukkit.getName() + "-" + MinecraftVersion.getAsText())
                .replace("%nms%", nmsVersion)
                .replace("%os%", SystemEnviroment.getOS())
                .replace("%jdk%", SystemEnviroment.getJDKVersion());
        System.out.println(str);
    }
}
