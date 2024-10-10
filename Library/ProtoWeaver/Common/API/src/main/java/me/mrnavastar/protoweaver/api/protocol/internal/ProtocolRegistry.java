package me.mrnavastar.protoweaver.api.protocol.internal;

public record ProtocolRegistry(String namespace, String key, boolean global, Class<?> packetType) {
}
