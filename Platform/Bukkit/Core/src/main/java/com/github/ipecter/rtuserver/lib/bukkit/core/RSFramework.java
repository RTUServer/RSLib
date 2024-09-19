package com.github.ipecter.rtuserver.lib.bukkit.core;

import com.github.ipecter.rtuserver.lib.bukkit.api.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.api.command.RSCommand;
import com.github.ipecter.rtuserver.lib.bukkit.api.listener.RSListener;
import com.github.ipecter.rtuserver.lib.bukkit.api.util.format.ComponentFormatter;
import com.github.ipecter.rtuserver.lib.bukkit.api.util.platform.MinecraftVersion;
import com.github.ipecter.rtuserver.lib.bukkit.api.util.platform.SystemEnvironment;
import com.github.ipecter.rtuserver.lib.bukkit.core.config.CommonTranslation;
import com.github.ipecter.rtuserver.lib.bukkit.core.internal.listeners.InventoryListener;
import com.github.ipecter.rtuserver.lib.bukkit.core.internal.listeners.JoinListener;
import com.github.ipecter.rtuserver.lib.bukkit.core.internal.runnable.CommandLimit;
import com.github.ipecter.rtuserver.lib.bukkit.core.modules.Modules;
import com.github.ipecter.rtuserver.lib.bukkit.nms.v1_17_r1.NMS_1_17_R1;
import com.github.ipecter.rtuserver.lib.bukkit.nms.v1_18_r1.NMS_1_18_R1;
import com.github.ipecter.rtuserver.lib.bukkit.nms.v1_18_r2.NMS_1_18_R2;
import com.github.ipecter.rtuserver.lib.bukkit.nms.v1_19_r1.NMS_1_19_R1;
import com.github.ipecter.rtuserver.lib.bukkit.nms.v1_19_r2.NMS_1_19_R2;
import com.github.ipecter.rtuserver.lib.bukkit.nms.v1_19_r3.NMS_1_19_R3;
import com.github.ipecter.rtuserver.lib.bukkit.nms.v1_20_r1.NMS_1_20_R1;
import com.github.ipecter.rtuserver.lib.bukkit.nms.v1_20_r2.NMS_1_20_R2;
import com.github.ipecter.rtuserver.lib.bukkit.nms.v1_20_r3.NMS_1_20_R3;
import com.github.ipecter.rtuserver.lib.bukkit.nms.v1_20_r4.NMS_1_20_R4;
import com.github.ipecter.rtuserver.lib.bukkit.nms.v1_21_r1.NMS_1_21_R1;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.callback.PacketCallback;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.impl.bukkit.core.BukkitProtoWeaver;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j(topic = "RSLib/Framework")
//@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RSFramework implements com.github.ipecter.rtuserver.lib.bukkit.api.core.RSFramework {

    @Getter
    private final Component prefix = ComponentFormatter.mini("<gradient:#00f260:#057eff>RSLib/Framework » </gradient>");
    @Getter
    private final Map<String, RSPlugin> plugins = new HashMap<>();
    @Getter
    private final Map<String, Boolean> hooks = new HashMap<>();
    private final PacketCallback callable = new PacketCallback(this::onPacket);
    @Getter
    private com.github.ipecter.rtuserver.lib.bukkit.api.nms.NMS NMS;
    @Getter
    private BukkitProtoWeaver protoWeaver;
    @Getter
    private String NMSVersion;
    @Getter
    private CommandLimit commandLimit;
    @Getter
    private CommonTranslation commonTranslation;
    @Getter
    private Modules modules;

    public void loadPlugin(RSPlugin plugin) {
        log.info("loading RSPlugin: {}", plugin.getName());
        plugins.put(plugin.getName(), plugin);
    }

    public void unloadPlugin(RSPlugin plugin) {
        log.info("unloading RSPlugin: {}", plugin.getName());
        plugins.remove(plugin.getName());
    }

    public boolean isEnabledDependency(String dependencyName) {
        return hooks.getOrDefault(dependencyName, false);
    }

    public void hookDependency(String dependencyName) {
        hooks.put(dependencyName, Bukkit.getPluginManager().isPluginEnabled(dependencyName));
    }

    public void load(RSPlugin plugin) {
        loadNMS(plugin);
    }

    private void loadNMS(RSPlugin plugin) {
        NMSVersion = MinecraftVersion.getNMS(MinecraftVersion.getAsText());
        switch (NMSVersion) {
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
                log.warn("Server version is unsupported version, Disabling RSLib...");
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
        }
        protoWeaver = new BukkitProtoWeaver(callable, plugin.getDataFolder().getPath(), NMSVersion);
    }

    private void onPacket(ProtoConnection connection, Object object) {
    }

    public void enable(RSPlugin plugin) {
        printStartUp(plugin);
        commonTranslation = new CommonTranslation(plugin);
        modules = new Modules(plugin);
        registerInternal(plugin);
    }

    public void disable(RSPlugin plugin) {
    }

    private void registerInternal(RSPlugin plugin) {
        registerInternalRunnable(plugin);
        registerInternalListener(plugin);
    }

    private void registerInternalRunnable(RSPlugin plugin) {
        commandLimit = new CommandLimit(plugin);
    }

    private void registerInternalListener(RSPlugin plugin) {
        registerEvent(new JoinListener(this, plugin));
        registerEvent(new InventoryListener(plugin));
    }

    private void printStartUp(RSPlugin plugin) {
        Audience audience = plugin.getAdventure().console();
        List<String> list = List.of(
                "╔ <gray>Developed by</gray> ════════════════════════════════════════════════════════════════════════════╗",
                "║ ░█▀▄░█░█░▀█▀░█▀█░█▀▀░█▀▄░░░▀█▀░█▀▀░█▀▀░█░█░█▀█░█▀█░█░░░█▀█░█▀▀░█░█░░░█░█░█▀█░▀█▀░█▀▀░█░█ ║",
                "║ ░█▀▄░█░█░░█░░█░█░█▀▀░█░█░░░░█░░█▀▀░█░░░█▀█░█░█░█░█░█░░░█░█░█░█░░█░░░░█░█░█░█░░█░░█▀▀░░█░ ║",
                "║ ░▀░▀░▀▀▀░▀▀▀░▀░▀░▀▀▀░▀▀░░░░░▀░░▀▀▀░▀▀▀░▀░▀░▀░▀░▀▀▀░▀▀▀░▀▀▀░▀▀▀░░▀░░░░▀▀▀░▀░▀░▀▀▀░▀░░░░▀░ ║",
                "╚══════════════════════════════════════════════════════════════════════════════════════════╝"
        );
        audience.sendMessage(ComponentFormatter.mini(
                "RSLib | Version: %s | Bukkit: %s | NMS: %s | OS: %s | JDK: %s"
                        .formatted(plugin.getDescription().getVersion()
                                , Bukkit.getName() + "-" + MinecraftVersion.getAsText()
                                , NMSVersion
                                , SystemEnvironment.getOS()
                                , SystemEnvironment.getJDKVersion())));
        for (String message : list)
            audience.sendMessage(ComponentFormatter.mini("<gradient:#2979FF:#7C4DFF>" + message + "</gradient>"));
    }

    public void registerEvent(RSListener listener) {
        Bukkit.getPluginManager().registerEvents(listener, listener.getPlugin());
    }

    public void registerCommand(RSCommand command) {
        NMS.commandMap().register(command.getName(), command);
    }

    public void registerPermission(String name, PermissionDefault permissionDefault) {
        Bukkit.getPluginManager().addPermission(new Permission(name, permissionDefault));
    }

    public void registerProtocol(String namespace, String key, Class<?> packetType, Class<? extends ProtoConnectionHandler> protocolHandler) {
        protoWeaver.registerProtocol(namespace, key, packetType, protocolHandler, null);
    }


}
