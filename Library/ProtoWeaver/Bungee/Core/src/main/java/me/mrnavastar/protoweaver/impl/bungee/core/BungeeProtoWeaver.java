package me.mrnavastar.protoweaver.impl.bungee.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.callback.HandlerCallback;
import me.mrnavastar.protoweaver.api.protocol.CompressionType;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.internal.ProtocolRegistry;
import me.mrnavastar.protoweaver.api.proxy.ProtoServer;
import me.mrnavastar.protoweaver.api.util.ProtoLogger;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.CommonPacketHandler;
import me.mrnavastar.protoweaver.core.proxy.ProtoProxy;
import me.mrnavastar.protoweaver.impl.bungee.api.BungeeProtoHandler;
import net.md_5.bungee.api.ProxyServer;

import java.nio.file.Path;
import java.util.List;

@Slf4j(topic = "RSLib/ProtoWeaver")
@Getter
public class BungeeProtoWeaver implements me.mrnavastar.protoweaver.impl.bungee.api.BungeeProtoWeaver {

    private final ProxyServer server;
    private final Protocol.Builder protocol;
    private final HandlerCallback callable = new HandlerCallback(null, this::onPacket);

    private final Path dir;

    private final ProtoProxy protoProxy;

    public BungeeProtoWeaver(ProxyServer server, Path dir) {
        this.server = server;
        this.dir = dir;
        this.protoProxy = new ProtoProxy(this, dir);
        ProtoLogger.setLogger(this);
        protocol = Protocol.create("rslib", "internal");
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb
        protocol.addPacket(Object.class);
        protocol.setClientHandler(BungeeProtoHandler.class, callable).load();
    }

    public void registerProtocol(ProtocolRegistry registry) {
        registerProtocol(registry.namespace(), registry.key(), registry.global(), registry.packetType(), CommonPacketHandler.class, null);
    }

    public void registerProtocol(String namespace, String key, boolean global, Class<?> packetType, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback) {
        Protocol.Builder protocol = Protocol.create(namespace, key);
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb
        protocol.addPacket(packetType);
        protocol.setGlobal(global);
        protocol.setClientHandler(protocolHandler, callback).load();
    }

    public void disable() {
        protoProxy.shutdown();
    }


    @Override
    public List<ProtoServer> getServers() {
        return server.getServersCopy().values().stream().map(server -> new ProtoServer(server.getName(), server.getSocketAddress())).toList();
    }

    @Override
    public void info(String message) {
        log.info(message);
    }

    @Override
    public void warn(String message) {
        log.warn(message);
    }

    @Override
    public void err(String message) {
        log.error(message);
    }

    private void onPacket(HandlerCallback.Packet data) {
        if (data.packet() instanceof ProtocolRegistry registry) registerProtocol(registry);
    }
}
