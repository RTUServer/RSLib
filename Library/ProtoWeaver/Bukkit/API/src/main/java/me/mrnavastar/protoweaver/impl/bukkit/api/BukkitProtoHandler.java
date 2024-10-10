package me.mrnavastar.protoweaver.impl.bukkit.api;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.callback.HandlerCallback;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;

@Slf4j(topic = "RSLib/ProtoHandler")
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
public class BukkitProtoHandler implements ProtoConnectionHandler {

    private static ProtoConnection proxy;
    private final HandlerCallback callable;

    public static ProtoConnection getProxy() {
        if (proxy == null || !proxy.isOpen()) return null;
        return proxy;
    }

    @Override
    public void onReady(ProtoConnection protoConnection) {
        if (callable != null) callable.onReady(protoConnection);
        proxy = protoConnection;
        log.info("Connected to Proxy");
        log.info("┠ Address: {}", protoConnection.getRemoteAddress());
        log.info("┖ Protocol: {}", protoConnection.getProtocol().getNamespaceKey());
    }

    @Override
    public void handlePacket(ProtoConnection protoConnection, Object packet) {
        if (callable != null) callable.handlePacket(protoConnection, packet);
    }

}
