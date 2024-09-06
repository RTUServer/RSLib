package me.mrnavastar.protoweaver.impl.api;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.proxy.ProxyServer;
import me.mrnavastar.protoweaver.api.core.util.ProtoLogger;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.proxy.ServerSupplier;

import java.nio.file.Path;

public interface VelocityProtoWeaver extends ProtoLogger.IProtoLogger, ServerSupplier {

    ProxyServer getServer();

    Protocol.Builder getProtocol();

    Toml getVelocityConfig();

    Path getDir();

    void onProxyInitialize();
}
