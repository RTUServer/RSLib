package kr.rtuserver.lib.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.extern.slf4j.Slf4j;
import me.mrnavastar.protoweaver.impl.velocity.api.VelocityProtoWeaver;

import java.nio.file.Path;

@Slf4j(topic = "RSLib")
public class RSLib {

    private final ProxyServer server;
    private final Path dir;
    private final VelocityProtoWeaver protoWeaver;

    @Inject
    public RSLib(ProxyServer server, @DataDirectory Path dir) {
        this.server = server;
        this.dir = dir;
        log.info("RSLib Velocity loaded.");
        protoWeaver = new me.mrnavastar.protoweaver.impl.velocity.core.VelocityProtoWeaver(server, dir.toAbsolutePath().getParent().getParent());
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        server.getEventManager().register(this, protoWeaver);
    }
}
