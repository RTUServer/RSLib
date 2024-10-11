package me.mrnavastar.protoweaver.impl.bukkit.api;

import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.callback.HandlerCallback;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.impl.bukkit.api.nms.IProtoWeaver;

import java.util.Map;

public interface BukkitProtoWeaver {

    IProtoWeaver getProtoWeaver();

    boolean isModernProxy();

    Map<String, Protocol> getProtocols();

    void registerProtocol(String namespace, String key, Class<?> packetType, boolean global, Class<? extends ProtoConnectionHandler> protocolHandler);

    void registerProtocol(String namespace, String key, Class<?> packetType, boolean global, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback);
}