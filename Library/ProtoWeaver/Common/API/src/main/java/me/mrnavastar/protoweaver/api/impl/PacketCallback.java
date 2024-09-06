package me.mrnavastar.protoweaver.api.impl;

import me.mrnavastar.protoweaver.api.netty.ProtoConnection;

public class PacketCallback {

    private final Callback<Object> callback;

    public PacketCallback(Callback<Object> callback) {
        this.callback = callback;
    }

    public void handlePacket(ProtoConnection protoConnection, Object packet) {
        callback.run(protoConnection, packet);
    }
}
