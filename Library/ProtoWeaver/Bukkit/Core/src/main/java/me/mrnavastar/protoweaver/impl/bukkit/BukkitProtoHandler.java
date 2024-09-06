package me.mrnavastar.protoweaver.impl.bukkit;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.impl.PacketCallback;

@Slf4j(topic = "RSLib/ProtoHandler")
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
public class BukkitProtoHandler implements ProtoConnectionHandler {

    private static ProtoConnection proxy;
    private final PacketCallback callable;

    public static ProtoConnection getProxy() {
        if (proxy == null || !proxy.isOpen()) return null;
        return proxy;
    }

    @Override
    public void onReady(ProtoConnection protoConnection) {
        proxy = protoConnection;
        log.info("Connected to Proxy({})", protoConnection.getRemoteAddress());
    }

    @Override
    public void handlePacket(ProtoConnection protoConnection, Object packet) {
        if (callable != null) callable.handlePacket(protoConnection, packet);
    }

}
