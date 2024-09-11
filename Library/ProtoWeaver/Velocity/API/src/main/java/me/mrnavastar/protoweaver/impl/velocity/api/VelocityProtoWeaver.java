package me.mrnavastar.protoweaver.impl.velocity.api;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.proxy.ProxyServer;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.proxy.ServerSupplier;
import me.mrnavastar.protoweaver.api.util.ProtoLogger;

import java.nio.file.Path;

public interface VelocityProtoWeaver extends ProtoLogger.IProtoLogger, ServerSupplier {

    ProxyServer getServer();

    Protocol.Builder getProtocol();

    Toml getVelocityConfig();

    Path getDir();

    void onProxyInitialize();
}
