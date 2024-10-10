package me.mrnavastar.protoweaver.impl.bukkit.api;

import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.callback.HandlerCallback;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.impl.bukkit.api.nms.IProtoWeaver;

import java.util.List;

public interface BukkitProtoWeaver {

    IProtoWeaver getProtoWeaver();

    boolean isModernProxy();

    List<Protocol> getProtocols();

    void registerProtocol(String namespace, String key, Class<?> packetType, Class<? extends ProtoConnectionHandler> protocolHandler);

    void registerProtocol(String namespace, String key, Class<?> packetType, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback);

    void registerProtocol(String namespace, String key, boolean global, Class<?> packetType, Class<? extends ProtoConnectionHandler> protocolHandler);

    void registerProtocol(String namespace, String key, boolean global, Class<?> packetType, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback);
}