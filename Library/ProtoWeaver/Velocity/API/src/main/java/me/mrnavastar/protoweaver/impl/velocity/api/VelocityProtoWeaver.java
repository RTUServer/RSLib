package me.mrnavastar.protoweaver.impl.velocity.api;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.proxy.ProxyServer;
import me.mrnavastar.protoweaver.api.callback.HandlerCallback;
import me.mrnavastar.protoweaver.api.protocol.internal.ProtocolRegistry;
import me.mrnavastar.protoweaver.api.proxy.ServerSupplier;
import me.mrnavastar.protoweaver.api.util.ProtoLogger;

import java.nio.file.Path;

public interface VelocityProtoWeaver extends ProtoLogger.IProtoLogger, ServerSupplier {

    ProxyServer getServer();

    Toml getVelocityConfig();

    Path getDir();

    void registerProtocol(ProtocolRegistry registry);

    void registerProtocol(String namespace, String key, boolean global, Class<?> packetType, HandlerCallback callback);
}
