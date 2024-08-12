package com.github.ipecter.rtuserver.lib.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;
import me.mrnavastar.protoweaver.proxy.ServerSupplier;
import me.mrnavastar.protoweaver.proxy.api.ProtoProxy;
import org.slf4j.Logger;

import java.net.SocketAddress;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class RSLib implements ServerSupplier, ProtoLogger.IProtoLogger {

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
        ProtoLogger.setLogger(this);
    }

    private ProtoConnectionHandler handler;

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        protoProxy = new ProtoProxy(this, dir);
        getServers().forEach(socketAddress -> System.out.println("??: " + socketAddress));
        handler = new Tesr();
    }

    @Subscribe
    public void onServer(ServerPreConnectEvent event) {
        System.out.println("Server Connection");
        ProtoProxy.sendAll("hi!!!");
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
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }

}
