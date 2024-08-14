package me.mrnavastar.protoweaver.impl;

import me.mrnavastar.protoweaver.api.protocol.CompressionType;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.velocity.VelocityAuth;

public class Protocols {

    public static final Protocol.Builder GLOBAL = Protocol.create("rslib", "global")
            .setServerAuthHandler(VelocityAuth.class)
            .setClientAuthHandler(VelocityAuth.class)
            .setCompression(CompressionType.SNAPPY)
            .setMaxPacketSize(67108864) // 64mb
            .addPacket(Object.class);

    public static final Protocol.Builder COMMON = Protocol.create("rslib", "common")
            .setServerAuthHandler(VelocityAuth.class)
            .setClientAuthHandler(VelocityAuth.class)
            .setCompression(CompressionType.SNAPPY)
            .setMaxPacketSize(67108864) // 64mb
            .addPacket(Object.class);
}
