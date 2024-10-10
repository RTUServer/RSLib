package me.mrnavastar.protoweaver.api.callback;

import lombok.RequiredArgsConstructor;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;

@RequiredArgsConstructor
public class HandlerCallback {

    private final Callback<Ready> readyCall;
    private final Callback<Packet> packetCall;

    public void onReady(ProtoConnection connection) {
        if (readyCall != null) readyCall.run(new Ready(connection));
    }

    public void handlePacket(ProtoConnection protoConnection, Object packet) {
        if (packetCall != null) packetCall.run(new Packet(protoConnection, packet));
    }

    public record Ready(ProtoConnection protoConnection) {
    }

    public record Packet(ProtoConnection protoConnection, Object packet) {
    }

    public interface Callback<T> {
        void run(T data);
    }
}
