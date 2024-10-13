package me.mrnavastar.protoweaver.impl.bungee.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.callback.HandlerCallback;
import me.mrnavastar.protoweaver.api.protocol.CompressionType;
import me.mrnavastar.protoweaver.api.protocol.Packet;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.internal.CustomPacket;
import me.mrnavastar.protoweaver.api.protocol.internal.ProtocolRegister;
import me.mrnavastar.protoweaver.api.protocol.velocity.VelocityAuth;
import me.mrnavastar.protoweaver.api.proxy.ProtoServer;
import me.mrnavastar.protoweaver.api.util.ProtoLogger;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.CommonPacketHandler;
import me.mrnavastar.protoweaver.core.proxy.ProtoProxy;
import me.mrnavastar.protoweaver.impl.bungee.api.BungeeProtoHandler;
import net.md_5.bungee.api.ProxyServer;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@Slf4j(topic = "RSLib/ProtoWeaver")
@Getter
public class BungeeProtoWeaver implements me.mrnavastar.protoweaver.impl.bungee.api.BungeeProtoWeaver {

    private final ProxyServer server;
    private final Protocol.Builder protocol;

    private final Path dir;

    private final ProtoProxy protoProxy;
    private final HandlerCallback callable = new HandlerCallback(null, this::onPacket);

    public BungeeProtoWeaver(ProxyServer server, Path dir) {
        this.server = server;
        this.dir = dir;
        this.protoProxy = new ProtoProxy(this, dir);
        ProtoLogger.setLogger(this);
        protocol = Protocol.create("rslib", "internal");
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb
        protocol.addPacket(ProtocolRegister.class);
        protocol.addPacket(Packet.class);
        protocol.setClientHandler(BungeeProtoHandler.class, callable).load();
    }

    public void registerProtocol(String namespace, String key, Packet packet, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback) {
        registerProtocol(namespace, key, Set.of(packet), protocolHandler, callback);
    }

    public void registerProtocol(String namespace, String key, Set<Packet> packets, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback) {
        Protocol.Builder protocol = Protocol.create(namespace, key);
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb
        for (Packet packet : packets) {
            if (packet.isBothSide()) protocol.addPacket(packet);
            else protocol.addPacket(Packet.of(CustomPacket.class, packet.isGlobal(), false));
        }
        protocol.setClientHandler(CommonPacketHandler.class, null).load();
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
        if (data.packet() instanceof ProtocolRegister register) {
            registerProtocol(register.namespace(), register.key(), register.packet(), CommonPacketHandler.class, null);
        }
    }
}
