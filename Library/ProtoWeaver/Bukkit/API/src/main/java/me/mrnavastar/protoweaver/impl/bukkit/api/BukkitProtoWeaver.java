package me.mrnavastar.protoweaver.impl.bukkit.api;

import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.callback.HandlerCallback;
import me.mrnavastar.protoweaver.api.protocol.Packet;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.impl.bukkit.api.nms.IProtoWeaver;

import java.util.Map;

public interface BukkitProtoWeaver {

    IProtoWeaver getProtoWeaver();

    boolean isModernProxy();

    void registerProtocol(String namespace, String key, Packet packet, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback);
}