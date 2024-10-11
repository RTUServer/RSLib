package me.mrnavastar.protoweaver.api.protocol;

import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.callback.HandlerCallback;

public record Request(String namespace, String key, Class<?> packetType, boolean global,
                      Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback) {

    public String namespaceKey() {
        return namespace + ":" + key;
    }

}
