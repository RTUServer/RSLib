package me.mrnavastar.protoweaver.impl.bungee;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.mrnavastar.protoweaver.api.protocol.CompressionType;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.velocity.VelocityAuth;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;
import me.mrnavastar.protoweaver.impl.PacketCallback;
import me.mrnavastar.protoweaver.proxy.ServerSupplier;
import me.mrnavastar.protoweaver.proxy.api.ProtoProxy;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j(topic = "RSLib/ProtoWeaver")
@Getter
public class BungeeProtoWeaver implements ProtoLogger.IProtoLogger {

    private final ProxyServer server;
    private final Protocol.Builder protocol;

    private ProtoProxy protoProxy;

    public BungeeProtoWeaver(PacketCallback callable, ProxyServer server) {
        this.server = server;
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

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
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
