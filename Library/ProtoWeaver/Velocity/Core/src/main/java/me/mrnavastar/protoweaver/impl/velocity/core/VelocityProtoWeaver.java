package me.mrnavastar.protoweaver.impl.velocity.core;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.event.proxy.server.ServerRegisteredEvent;
import com.velocitypowered.api.event.proxy.server.ServerUnregisteredEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.mrnavastar.protoweaver.api.callback.HandlerCallback;
import me.mrnavastar.protoweaver.api.protocol.CompressionType;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.internal.ProtocolRegistry;
import me.mrnavastar.protoweaver.api.protocol.velocity.VelocityAuth;
import me.mrnavastar.protoweaver.api.proxy.ProtoServer;
import me.mrnavastar.protoweaver.api.util.ProtoLogger;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.CommonPacketHandler;
import me.mrnavastar.protoweaver.core.proxy.ProtoProxy;
import me.mrnavastar.protoweaver.impl.velocity.api.VelocityProtoHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j(topic = "RSLib/ProtoWeaver")
@Getter
public class VelocityProtoWeaver implements me.mrnavastar.protoweaver.impl.velocity.api.VelocityProtoWeaver {

    private final ProxyServer server;
    private final Protocol.Builder protocol;
    private final HandlerCallback callable = new HandlerCallback(null, this::onPacket);

    private final Toml velocityConfig;
    private final Path dir;

    private final ProtoProxy protoProxy;

    public VelocityProtoWeaver(ProxyServer server, Path dir) {
        this.server = server;
        this.dir = dir;
        this.protoProxy = new ProtoProxy(this, dir);
        ProtoLogger.setLogger(this);
        velocityConfig = new Toml().read(new File(dir.toFile(), "velocity.toml"));
        protocol = Protocol.create("rslib", "internal");
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb
        protocol.addPacket(Object.class);
        protocol.addPacket(ProtocolRegistry.class);
        if (isModernProxy()) {
            info("Detected modern proxy");
            protocol.setServerAuthHandler(VelocityAuth.class);
            protocol.setClientAuthHandler(VelocityAuth.class);
        }
        protocol.setClientHandler(VelocityProtoHandler.class, callable).load();
    }


    public void registerProtocol(ProtocolRegistry registry) {
        registerProtocol(registry.namespace(), registry.key(), registry.global(), registry.packetType(), null);
    }

    public void registerProtocol(String namespace, String key, boolean global, Class<?> packetType, HandlerCallback callback) {
        Protocol.Builder protocol = Protocol.create(namespace, key);
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb
        //protocol.addPacket(packetType);
        if (isModernProxy()) {
            protocol.setServerAuthHandler(VelocityAuth.class);
            protocol.setClientAuthHandler(VelocityAuth.class);
        }
        protocol.setGlobal(global);
        protocol.setClientHandler(CommonPacketHandler.class, callback).load();
    }

    private boolean isModernProxy() {
        String mode = velocityConfig.getString("player-info-forwarding-mode", "");
        if (!List.of("modern", "bungeeguard").contains(mode.toLowerCase())) return false;
        String secretPath = velocityConfig.getString("forwarding-secret-file", "");
        if (secretPath.isEmpty()) return false;
        File file = new File(dir.toFile(), secretPath);
        if (!file.exists() || !file.isFile()) return false;
        try {
            String key = String.join("", Files.readAllLines(file.toPath()));
            return !key.isEmpty();
        } catch (IOException e) {
            return false;
        }
    }

    @Subscribe
    public void onRegister(ServerRegisteredEvent event) {
        ServerInfo server = event.registeredServer().getServerInfo();
        protoProxy.register(new ProtoServer(server.getName(), server.getAddress()));
    }

    @Subscribe
    public void onUnregister(ServerUnregisteredEvent event) {
        ServerInfo server = event.unregisteredServer().getServerInfo();
        protoProxy.unregister(new ProtoServer(server.getName(), server.getAddress()));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        protoProxy.shutdown();
    }

    @Override
    public List<ProtoServer> getServers() {
        return server.getAllServers().stream().map(server -> new ProtoServer(server.getServerInfo().getName(), server.getServerInfo().getAddress())).toList();
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
