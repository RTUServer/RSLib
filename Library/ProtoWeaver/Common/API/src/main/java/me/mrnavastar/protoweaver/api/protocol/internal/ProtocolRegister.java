package me.mrnavastar.protoweaver.api.protocol.internal;

import me.mrnavastar.protoweaver.api.protocol.Packet;

import java.util.Set;

public record ProtocolRegister(String namespace, String key, Set<Packet> packet) {
    public ProtocolRegister(String namespace, String key, Packet packet) {
        this(namespace, key, Set.of(packet));
    }
}