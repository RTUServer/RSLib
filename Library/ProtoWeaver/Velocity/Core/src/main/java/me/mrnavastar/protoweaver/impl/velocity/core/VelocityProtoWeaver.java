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
        protocol.addPacket(RegisterRequest.class);
        protocol.addPacket(RegisterResponse.class);
        if (isModernProxy()) {
            info("Detected modern proxy");
            protocol.setServerAuthHandler(VelocityAuth.class);
            protocol.setClientAuthHandler(VelocityAuth.class);
        }
        protocol.setClientHandler(VelocityProtoHandler.class, callable).load();
    }


    public void registerProtocol(Request request) {
        registerProtocol(request.namespace(), request.key(), request.packetType(), request.global(), request.protocolHandler(), null);
    }

    public void registerProtocol(String namespace, String key, Class<?> packetType, boolean global, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback) {
        Protocol.Builder protocol = Protocol.create(namespace, key);
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb
        protocol.addPacket(PacketType.of(packetType, global));
        if (isModernProxy()) {
            protocol.setServerAuthHandler(VelocityAuth.class);
            protocol.setClientAuthHandler(VelocityAuth.class);
        }
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
        if (data.packet() instanceof RegisterRequest request) {
            boolean findClass = findClass(request.getType());
            boolean alreadyLoaded = ProtoWeaver.getLoadedProtocols().stream().anyMatch(p -> p.getNamespaceKey().equalsIgnoreCase(request.getNamespaceKey()));
            System.out.println(alreadyLoaded + "/" + request.getNamespaceKey());
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
