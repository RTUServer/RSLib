package me.mrnavastar.protoweaver.impl.bukkit;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.impl.PacketCallback;

@Slf4j(topic = "ProtoHandler")
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
public class BukkitProtoHandler implements ProtoConnectionHandler {

    private static ProtoConnection proxy;

    public static ProtoConnection getProxy() {
        if (proxy == null || !proxy.isOpen()) return null;
        return proxy;
    }

    private final PacketCallback callable;

    @Override
    public void onReady(ProtoConnection protoConnection) {
        proxy = protoConnection;
        log.info("Connected to {}", protoConnection.getRemoteAddress());
    }

    @Override
    public void handlePacket(ProtoConnection protoConnection, Object packet) {
        log.info("Packet! {}", packet.toString());
        callable.handlePacket(protoConnection, packet);
    }

}
