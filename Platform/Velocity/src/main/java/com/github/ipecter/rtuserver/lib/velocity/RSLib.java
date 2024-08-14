package com.github.ipecter.rtuserver.lib.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.impl.PacketCallback;
import me.mrnavastar.protoweaver.impl.velocity.VelocityProtoWeaver;
import org.slf4j.Logger;

import java.nio.file.Path;

public class RSLib {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dir;

    private VelocityProtoWeaver protoWeaver;

    private final PacketCallback callable = new PacketCallback(this::onPacket);

    @Inject
    public RSLib(ProxyServer server, Logger logger, @DataDirectory Path dir) {
        this.server = server;
        this.logger = logger;
        this.dir = dir;
        logger.info("RSLib Velocity loaded.");

        protoWeaver = new VelocityProtoWeaver(callable, server, dir.toAbsolutePath().getParent().getParent());
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        server.getEventManager().register(this, protoWeaver);
        protoWeaver.onProxyInitialize();
    }


    private void onPacket(ProtoConnection connection, Object object) {
        System.out.println("onPacket: " + connection.getRemoteAddress() + " / " + object);
    }
}
