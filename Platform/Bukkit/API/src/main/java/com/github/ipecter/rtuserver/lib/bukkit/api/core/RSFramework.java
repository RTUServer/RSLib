package com.github.ipecter.rtuserver.lib.bukkit.api.core;

import com.github.ipecter.rtuserver.lib.bukkit.api.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.api.command.RSCommand;
import com.github.ipecter.rtuserver.lib.bukkit.api.core.internal.runnable.CommandLimit;
import com.github.ipecter.rtuserver.lib.bukkit.api.listener.RSListener;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.impl.bukkit.BukkitProtoWeaver;
import net.kyori.adventure.text.Component;
import org.bukkit.permissions.PermissionDefault;

import java.util.Map;

public interface RSFramework {
    Component getPrefix();

    Map<String, RSPlugin> getPlugins();

    Map<String, Boolean> getHooks();

    com.github.ipecter.rtuserver.lib.bukkit.api.nms.NMS getNMS();

    BukkitProtoWeaver getProtoWeaver();

    String getNMSVersion();

    CommandLimit getCommandLimit();

    void loadPlugin(RSPlugin plugin);

    void unloadPlugin(RSPlugin plugin);

    boolean isEnabledDependency(String dependencyName);

    void hookDependency(String dependencyName);

    void load(RSPlugin plugin);

    void enable(RSPlugin plugin);

    void disable(RSPlugin plugin);

    void registerInternalRunnable(RSPlugin plugin);

    void registerEvent(RSListener listener);

    void registerCommand(RSCommand command);

    void registerPermission(String name, PermissionDefault permissionDefault);

    void registerProtocol(String namespace, String key, Class<?> packetType, Class<? extends ProtoConnectionHandler> protocolHandler);

}
