package me.mrnavastar.protoweaver.impl.bukkit.api;

import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.callback.PacketCallback;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.impl.bukkit.api.nms.IProtoWeaver;

import java.util.List;

public interface BukkitProtoWeaver {

    IProtoWeaver getProtoWeaver();

    boolean isModernProxy();

    List<Protocol> getProtocols();

    void registerProtocol(String namespace, String key, Class<?> packetType, Class<? extends ProtoConnectionHandler> protocolHandler, PacketCallback callback);
}