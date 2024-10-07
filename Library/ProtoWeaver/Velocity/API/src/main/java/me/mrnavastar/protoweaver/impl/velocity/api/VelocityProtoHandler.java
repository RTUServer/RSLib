package me.mrnavastar.protoweaver.impl.velocity.api;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.callback.PacketCallback;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;

import java.util.ArrayList;
import java.util.List;

@Slf4j(topic = "RSLib/ProtoHandler")
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
public class VelocityProtoHandler implements ProtoConnectionHandler {

    private static ProtoConnection server;
    private static final List<ProtoConnection> servers = new ArrayList<>();
    private final PacketCallback callable;

    public static ProtoConnection getServer() {
        if (server == null || !server.isOpen()) return null;
        return server;
    }

    public static List<ProtoConnection> getServers() {
        List<ProtoConnection> result = new ArrayList<>();
        for (ProtoConnection server : servers) {
            if (server.isOpen()) result.add(server);
            else servers.remove(server);
        }
        return result;
    }

    @Override
    public void onReady(ProtoConnection protoConnection) {
        server = protoConnection;
        servers.add(protoConnection);
        log.info("Connected to Server({})", protoConnection.getRemoteAddress());
    }

    @Override
    public void handlePacket(ProtoConnection protoConnection, Object packet) {
        if (callable != null) callable.handlePacket(protoConnection, packet);
        if (protoConnection.getProtocol().isGlobal()) getServers().forEach(connection -> connection.send(packet));
    }
}
