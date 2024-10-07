package me.mrnavastar.protoweaver.impl.velocity.core;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.mrnavastar.protoweaver.api.callback.PacketCallback;
import me.mrnavastar.protoweaver.api.protocol.CompressionType;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.velocity.VelocityAuth;
import me.mrnavastar.protoweaver.api.util.ProtoLogger;
import me.mrnavastar.protoweaver.core.proxy.ProtoProxy;
import me.mrnavastar.protoweaver.impl.velocity.api.VelocityProtoHandler;

import java.io.File;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j(topic = "RSLib/ProtoWeaver")
@Getter
public class VelocityProtoWeaver implements me.mrnavastar.protoweaver.impl.velocity.api.VelocityProtoWeaver {

    private final ProxyServer server;
    private final Protocol.Builder protocol;
    private final Toml velocityConfig;
    private final Path dir;

    private ProtoProxy protoProxy;

    public VelocityProtoWeaver(PacketCallback callable, ProxyServer server, Path dir) {
        this.server = server;
        this.dir = dir;
        ProtoLogger.setLogger(this);
        velocityConfig = new Toml().read(new File(dir.toFile(), "velocity.toml"));
        protocol = Protocol.create("rslib", "internal");
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb
        protocol.addPacket(Object.class);
        if (isModernProxy()) {
            info("Detected modern proxy");
            protocol.setServerAuthHandler(VelocityAuth.class);
            protocol.setClientAuthHandler(VelocityAuth.class);
        }
        protocol.setClientHandler(VelocityProtoHandler.class, callable).load();
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
        return server.getAllServers().stream()
                .map(server -> server.getServerInfo().getAddress())
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
