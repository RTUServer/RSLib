package me.mrnavastar.protoweaver.impl.velocity.core;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.callback.PacketCallback;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;

@Slf4j(topic = "RSLib/ProtoHandler")
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
public class VelocityProtoHandler implements ProtoConnectionHandler {

    private static ProtoConnection server;
    private final PacketCallback callable;

    public static ProtoConnection getServer() {
        if (server == null || !server.isOpen()) return null;
        return server;
    }

    @Override
    public void onReady(ProtoConnection protoConnection) {
        server = protoConnection;
        log.info("Connected to Server({})", protoConnection.getRemoteAddress());
    }

    @Override
    public void handlePacket(ProtoConnection protoConnection, Object packet) {
        if (callable != null) callable.handlePacket(protoConnection, packet);
    }
}
