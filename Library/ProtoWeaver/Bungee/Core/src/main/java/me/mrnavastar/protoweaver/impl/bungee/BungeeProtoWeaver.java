package me.mrnavastar.protoweaver.impl.bungee;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.mrnavastar.protoweaver.api.protocol.CompressionType;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.core.util.ProtoLogger;
import me.mrnavastar.protoweaver.api.impl.PacketCallback;
import me.mrnavastar.protoweaver.api.proxy.ServerSupplier;
import me.mrnavastar.protoweaver.api.proxy.api.ProtoProxy;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Listener;

import java.net.SocketAddress;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j(topic = "RSLib/ProtoWeaver")
@Getter
public class BungeeProtoWeaver implements Listener, ProtoLogger.IProtoLogger, ServerSupplier {

    private final ProxyServer server;
    private final Protocol.Builder protocol;
    private final Path dir;

    private ProtoProxy protoProxy;

    public BungeeProtoWeaver(PacketCallback callable, ProxyServer server, Path dir) {
        this.server = server;
        this.dir = dir;
        ProtoLogger.setLogger(this);
        protocol = Protocol.create("rslib", "internal");
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb
        protocol.addPacket(Object.class);
        protocol.setClientHandler(BungeeProtoHandler.class, callable).load();
    }

    public void onProxyInitialize() {
        protoProxy = new ProtoProxy(this, dir);
    }

    public void disable() {
        protoProxy.shutdown();
        protoProxy = null;
    }


    @Override
    public List<SocketAddress> getServers() {
        return server.getServersCopy().values().stream()
                .map(ServerInfo::getSocketAddress)
                .collect(Collectors.toList());
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
    public void error(String message) {
        log.error(message);
    }
}
