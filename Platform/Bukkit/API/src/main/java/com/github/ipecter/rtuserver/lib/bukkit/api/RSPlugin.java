package com.github.ipecter.rtuserver.lib.bukkit.api;

import com.github.ipecter.rtuserver.lib.bukkit.api.command.RSCommand;
import com.github.ipecter.rtuserver.lib.bukkit.api.config.impl.Configurations;
import com.github.ipecter.rtuserver.lib.bukkit.api.core.RSFramework;
import com.github.ipecter.rtuserver.lib.bukkit.api.listener.RSListener;
import com.github.ipecter.rtuserver.lib.bukkit.api.storage.Storage;
import com.github.ipecter.rtuserver.lib.bukkit.api.utility.format.ComponentFormatter;
import com.github.ipecter.rtuserver.lib.bukkit.api.utility.platform.MinecraftVersion;
import com.google.inject.Inject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public abstract class RSPlugin extends JavaPlugin {

    @Getter
    @Inject
    private RSFramework framework;
    @Getter
    private final Component prefix;
    private final Set<Listener> registeredListeners = new HashSet<>();
    @Getter
    private RSPlugin plugin;
    @Getter
    private BukkitAudiences adventure;
    @Getter
    private Configurations configurations;
    @Getter
    @Setter
    private Storage storage;

    public RSPlugin() {
        this.prefix = ComponentFormatter.mini("<gradient:#00f260:#057eff>" + getName() + " » </gradient>");
    }

    @Override
    public void onEnable() {
        if (MinecraftVersion.isSupport("1.17.1")) {
            plugin = this;
            adventure = BukkitAudiences.create(this);
        } else {
            Bukkit.getLogger().warning("Server version is unsupported version (< 1.17.1), Disabling this plugin...");
            Bukkit.getLogger().warning("서버 버전이 지원되지 않는 버전입니다 (< 1.17.1), 플러그인을 비활성화합니다...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        registerPermission(plugin.getName() + ".reload", PermissionDefault.OP);
        //for (String plugin : this.getDescription().getSoftDepend()) RSLib.getInstance().hookDependency(plugin); TODO: RSFramework
        configurations = new Configurations(this);
        enable();
        console("<green>활성화!</green>");
        //RSLib.getInstance().loadPlugin(this); TODO: RSFramework
    }

    @Override
    public void onDisable() {
        disable();
        if (storage != null) storage.close();
        //RSLib.getInstance().unloadPlugin(this); TODO: RSFramework
        console("<red>비활성화!</red>");
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
    }

    @Override
    public void onLoad() {
        load();
    }

    public void console(Component message) {
        getAdventure().console().sendMessage(getPrefix().append(Component.text(" ")).append(message));
    }

    public void console(String minimessage) {
        getAdventure().console().sendMessage(getPrefix().append(Component.text(" ")).append(ComponentFormatter.mini(minimessage)));
    }

    public void registerEvent(RSListener listener) {
        this.registeredListeners.add(listener);
        Bukkit.getPluginManager().registerEvents(listener, this);
    }


    public void registerEvents() {
        for (Listener listener : registeredListeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }

    public void unregisterEvents() {
        for (HandlerList handler : HandlerList.getHandlerLists()) {
            handler.unregister(this);
        }
    }

    public void registerCommand(RSCommand command) {
        framework.registerCommand(command);
    }

    public void registerPermission(String name, PermissionDefault permissionDefault) {
        framework.registerPermission(name, permissionDefault);
    }

    protected void registerProtocol(String namespace, String key, Class<?> packetType, Class<? extends ProtoConnectionHandler> protocolHandler) {
        framework.registerProtocol(namespace, key, packetType, protocolHandler);
    }

    protected void load() {
    }

    protected void enable() {
    }

    protected void disable() {
    }

    /***
     * Listener of Proxy Packet using Internal ProtoWeaver
     * 내장 ProtoWeaver를 사용한 프록시 패킷 리스너
     */
    protected void onPacket(ProtoConnection connection, Object object) {
    }

}