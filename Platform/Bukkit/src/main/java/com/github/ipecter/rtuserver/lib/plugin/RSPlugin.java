package com.github.ipecter.rtuserver.lib.plugin;

import com.github.ipecter.rtuserver.lib.bukkit.RSLib;
import com.github.ipecter.rtuserver.lib.plugin.command.RSCommand;
import com.github.ipecter.rtuserver.lib.plugin.config.Configurations;
import com.github.ipecter.rtuserver.lib.plugin.listener.RSListener;
import com.github.ipecter.rtuserver.lib.plugin.storage.Storage;
import com.github.ipecter.rtuserver.lib.bukkit.util.common.ComponentUtil;
import com.github.ipecter.rtuserver.lib.bukkit.util.common.VersionUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.impl.bukkit.BukkitProtoWeaver;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public abstract class RSPlugin extends JavaPlugin {

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
        this.prefix = ComponentUtil.miniMessage("<gradient:#00f260:#057eff>" + getName() + " » </gradient>");
    }

    @Override
    public void onEnable() {
        if (VersionUtil.isSupportVersion("1.17.1")) {
            plugin = this;
            adventure = BukkitAudiences.create(this);
        } else {
            Bukkit.getLogger().warning("Server version is unsupported version (< 1.17.1), Disabling this plugin...");
            Bukkit.getLogger().warning("서버 버전이 지원되지 않는 버전입니다 (< 1.17.1), 플러그인을 비활성화합니다...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        registerPermission(plugin.getName() + ".reload", PermissionDefault.OP);
        for (String plugin : this.getDescription().getSoftDepend()) RSLib.getInstance().hookDependency(plugin);
        configurations = new Configurations(this);
        enable();
        console(ComponentUtil.miniMessage("<green>활성화!</green>"));
        RSLib.getInstance().loadPlugin(this);
    }

    @Override
    public void onDisable() {
        disable();
        if (storage != null) storage.close();
        RSLib.getInstance().unloadPlugin(this);
        console(ComponentUtil.miniMessage("<red>비활성화!</red>"));
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
        RSLib.getInstance().getNMS().commandMap().register(command.getName(), command);
    }

    public void registerPermission(String name, PermissionDefault permissionDefault) {
        Bukkit.getPluginManager().addPermission(new Permission(name, permissionDefault));
    }

    protected void registerProtocol(String namespace, String key, Class<?> packetType, Class<? extends ProtoConnectionHandler> protocolHandler) {
        RSLib.getInstance().getProtoWeaver().registerProtocol(namespace, key, packetType, protocolHandler, null);
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