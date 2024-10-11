package me.mrnavastar.protoweaver.impl.bungee.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.callback.HandlerCallback;
import me.mrnavastar.protoweaver.api.protocol.CompressionType;
import me.mrnavastar.protoweaver.api.protocol.PacketType;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.Request;
import me.mrnavastar.protoweaver.api.protocol.internal.RegisterRequest;
import me.mrnavastar.protoweaver.api.protocol.internal.RegisterResponse;
import me.mrnavastar.protoweaver.api.protocol.internal.Result;
import me.mrnavastar.protoweaver.api.protocol.velocity.VelocityAuth;
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

    public void registerProtocol(Request request) {
        registerProtocol(request.namespace(), request.key(), request.packetType(), request.global(), request.protocolHandler(), null);
    }

    public void registerProtocol(String namespace, String key, Class<?> packetType, boolean global, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback) {
        Protocol.Builder protocol = Protocol.create(namespace, key);
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb
        protocol.addPacket(PacketType.of(packetType, global));
        protocol.setClientHandler(CommonPacketHandler.class, callback).load();
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
        if (data.packet() instanceof RegisterRequest request) {
            boolean findClass = findClass(request.getType());
            boolean alreadyLoaded = ProtoWeaver.getLoadedProtocols().stream().anyMatch(p -> p.getNamespaceKey().equalsIgnoreCase(request.getNamespaceKey()));
            data.protoConnection().send(new RegisterResponse(request.getNamespace(), request.getKey(), request.getType(), request.isGlobal(), new Result(alreadyLoaded, !findClass)));
        }
        if (data.packet() instanceof RegisterResponse response) {
            boolean findClass = findClass(response.getType());
            Class<?> type = findClass ? response.getClassType() : String.class;
            registerProtocol(response.getNamespace(), response.getKey(), type, response.isGlobal(), CommonPacketHandler.class, null);
        }
    }

    private boolean findClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
