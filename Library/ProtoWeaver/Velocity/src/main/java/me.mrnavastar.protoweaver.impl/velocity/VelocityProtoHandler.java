package me.mrnavastar.protoweaver.impl.velocity;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.impl.PacketCallback;

@Slf4j(topic = "ProtoHandler")
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
public class VelocityProtoHandler implements ProtoConnectionHandler {

    private static ProtoConnection server;

    public static ProtoConnection getServer() {
        if (server == null || !server.isOpen()) return null;
        return server;
    }

    private final PacketCallback callable;

    @Override
    public void onReady(ProtoConnection protoConnection) {
        server = protoConnection;
        log.info("Connected to {}", protoConnection.getRemoteAddress());
    }

    @Override
    public void handlePacket(ProtoConnection protoConnection, Object packet) {
        log.info("Packet! {}", packet.toString());
        if (callable != null) callable.handlePacket(protoConnection, packet);
    }
}
