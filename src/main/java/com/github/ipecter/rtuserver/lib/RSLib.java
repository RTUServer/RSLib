package com.github.ipecter.rtuserver.lib;

import com.github.ipecter.rtuserver.lib.commands.Command;
import com.github.ipecter.rtuserver.lib.listeners.MotdOnJoin;
import com.github.ipecter.rtuserver.lib.modules.Modules;
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
import com.github.ipecter.rtuserver.lib.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.plugin.inventory.RSInventoryListener;
import com.github.ipecter.rtuserver.lib.util.common.ComponentUtil;
import com.github.ipecter.rtuserver.lib.util.common.VersionUtil;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class RSLib extends RSPlugin {

    @Getter
    private static RSLib instance;
    @Getter
    private com.github.ipecter.rtuserver.lib.nms.NMS NMS;
    @Getter
    private final Map<String, RSPlugin> plugins = new HashMap<>();
    private final Map<String, Boolean> hooks = new HashMap<>();
    private String nmsVersion;
    @Getter
    private Modules modules;

    public RSLib() {
        super(ComponentUtil.miniMessage("<gradient:#00f260:#057eff>RSLib » </gradient>"));
    }

    public void loadPlugin(RSPlugin plugin) {
        console(ComponentUtil.miniMessage("<white>loading RSPlugin: " + plugin.getName() + "</white>"));
        plugins.put(plugin.getName(), plugin);
    }

    public void unloadPlugin(RSPlugin plugin) {
        console(ComponentUtil.miniMessage("<white>unloading RSPlugin: " + plugin.getName() + "</white>"));
        plugins.remove(plugin.getName());
    }

    public boolean isEnabledDependency(String dependencyName) {
        return hooks.getOrDefault(dependencyName, false);
    }

    public void hookDependency(String dependencyName) {
        hooks.put(dependencyName, Bukkit.getPluginManager().isPluginEnabled(dependencyName));
    }

    @Override
    public void load() {
        instance = this;
        loadNMS();
    }

    private void loadNMS() {
        nmsVersion = VersionUtil.getNMSVersion(VersionUtil.getVersionStr());
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
            default -> {
                Bukkit.getLogger().warning("Server version is unsupported version, Disabling RSLib...");
                this.getServer().getPluginManager().disablePlugin(this);
            }
        }
    }

    @Override
    public void enable() {
        console(ComponentUtil.miniMessage("<white>NMS: " + nmsVersion + "</white>"));
        modules = new Modules();
        registerEvent(new MotdOnJoin());
        registerEvent(new RSInventoryListener());
        registerCommand(new Command());
    }

    @Override
    public void disable() {
    }
}
