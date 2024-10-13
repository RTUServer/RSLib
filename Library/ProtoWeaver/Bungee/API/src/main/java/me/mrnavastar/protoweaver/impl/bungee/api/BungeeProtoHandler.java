package me.mrnavastar.protoweaver.impl.bungee.api;

import com.google.common.collect.ImmutableList;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.callback.HandlerCallback;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;

import java.util.ArrayList;
import java.util.List;

@Slf4j(topic = "RSLib/ProtoHandler")
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
public class BungeeProtoHandler implements ProtoConnectionHandler {

    private static final List<ProtoConnection> servers = new ArrayList<>();
    private static ProtoConnection server;
    private final HandlerCallback callable;

    public static ProtoConnection getServer() {
        if (server == null || !server.isOpen()) return null;
        return server;
    }

    public static List<ProtoConnection> getServers() {
        List<ProtoConnection> result = new ArrayList<>();
        for (ProtoConnection server : ImmutableList.copyOf(servers)) {
            if (server.isOpen()) result.add(server);
            else servers.remove(server);
        }
        return result;
    }

    @Override
    public void onReady(ProtoConnection protoConnection) {
        server = protoConnection;
        servers.add(protoConnection);
        log.info("Connected to Server");
        log.info("┠ Address: {}", protoConnection.getRemoteAddress());
        log.info("┖ Protocol: {}", protoConnection.getProtocol().getNamespaceKey());
        if (callable != null) callable.onReady(protoConnection);
    }

    @Override
    public void handlePacket(ProtoConnection protoConnection, Object packet) {
        if (callable != null) callable.handlePacket(protoConnection, packet);
        if (protoConnection.getProtocol().isGlobal(packet)) getServers().forEach(connection -> connection.send(packet));
    }
}
