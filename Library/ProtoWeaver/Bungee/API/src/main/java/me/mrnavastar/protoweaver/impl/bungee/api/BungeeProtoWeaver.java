package me.mrnavastar.protoweaver.impl.bungee.api;

import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.callback.HandlerCallback;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.Request;
import me.mrnavastar.protoweaver.api.proxy.ServerSupplier;
import me.mrnavastar.protoweaver.api.util.ProtoLogger;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;

import java.nio.file.Path;

public interface BungeeProtoWeaver extends Listener, ProtoLogger.IProtoLogger, ServerSupplier {

    ProxyServer getServer();

    Protocol.Builder getProtocol();

    Path getDir();

    void disable();

    void registerProtocol(Request request);

    void registerProtocol(String namespace, String key, Class<?> packetType, boolean global, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback);
}
