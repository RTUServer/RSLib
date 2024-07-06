package com.github.ipecter.rtuserver.lib.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.mrnavastar.protoweaver.proxy.ServerSupplier;
import me.mrnavastar.protoweaver.proxy.api.ProtoProxy;

import java.net.SocketAddress;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RSLib implements ServerSupplier {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dir;

    private ProtoProxy protoProxy;

    @Inject
    public RSLib(ProxyServer server, Logger logger, @DataDirectory Path dir) {
        this.server = server;
        this.logger = logger;
        this.dir = dir;
        logger.info("RSLib Velocity loaded.");
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
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

}
