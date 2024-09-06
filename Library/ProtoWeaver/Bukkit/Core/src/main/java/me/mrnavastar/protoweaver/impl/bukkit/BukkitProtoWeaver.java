package me.mrnavastar.protoweaver.impl.bukkit;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.protocol.CompressionType;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.velocity.VelocityAuth;
import me.mrnavastar.protoweaver.api.impl.PacketCallback;
import me.mrnavastar.protoweaver.impl.bukkit.nms.IProtoWeaver;
import me.mrnavastar.protoweaver.impl.bukkit.nms.v1_17_r1.ProtoWeaver_1_17_R1;
import me.mrnavastar.protoweaver.impl.bukkit.nms.v1_18_r1.ProtoWeaver_1_18_R1;
import me.mrnavastar.protoweaver.impl.bukkit.nms.v1_18_r2.ProtoWeaver_1_18_R2;
import me.mrnavastar.protoweaver.impl.bukkit.nms.v1_19_r1.ProtoWeaver_1_19_R1;
import me.mrnavastar.protoweaver.impl.bukkit.nms.v1_19_r2.ProtoWeaver_1_19_R2;
import me.mrnavastar.protoweaver.impl.bukkit.nms.v1_19_r3.ProtoWeaver_1_19_R3;
import me.mrnavastar.protoweaver.impl.bukkit.nms.v1_20_r1.ProtoWeaver_1_20_R1;
import me.mrnavastar.protoweaver.impl.bukkit.nms.v1_20_r2.ProtoWeaver_1_20_R2;
import me.mrnavastar.protoweaver.impl.bukkit.nms.v1_20_r3.ProtoWeaver_1_20_R3;
import me.mrnavastar.protoweaver.impl.bukkit.nms.v1_20_r4.ProtoWeaver_1_20_R4;
import me.mrnavastar.protoweaver.impl.bukkit.nms.v1_21_r1.ProtoWeaver_1_21_R1;

import java.util.ArrayList;
import java.util.List;

@Slf4j(topic = "RSLib/ProtoWeaver")
@Getter
public class BukkitProtoWeaver {

    private final IProtoWeaver protoWeaver;
    private final boolean isModernProxy;
    private final List<Protocol> protocols = new ArrayList<>();

    public BukkitProtoWeaver(PacketCallback callback, String sslFolder, String nmsVersion) {
        this.protoWeaver = switch (nmsVersion) {
            case "v1_17_R1" -> new ProtoWeaver_1_17_R1(sslFolder);
            case "v1_18_R1" -> new ProtoWeaver_1_18_R1(sslFolder);
            case "v1_18_R2" -> new ProtoWeaver_1_18_R2(sslFolder);
            case "v1_19_R1" -> new ProtoWeaver_1_19_R1(sslFolder);
            case "v1_19_R2" -> new ProtoWeaver_1_19_R2(sslFolder);
            case "v1_19_R3" -> new ProtoWeaver_1_19_R3(sslFolder);
            case "v1_20_R1" -> new ProtoWeaver_1_20_R1(sslFolder);
            case "v1_20_R2" -> new ProtoWeaver_1_20_R2(sslFolder);
            case "v1_20_R3" -> new ProtoWeaver_1_20_R3(sslFolder);
            case "v1_20_R4" -> new ProtoWeaver_1_20_R4(sslFolder);
            case "v1_21_R1" -> new ProtoWeaver_1_21_R1(sslFolder);
            default -> throw new IllegalStateException();
        };
        this.isModernProxy = protoWeaver.isModernProxy();
        registerProtocol("rslib", "internal", Object.class, BukkitProtoHandler.class, callback);
    }

    public void registerProtocol(String namespace, String key, Class<?> packetType, Class<? extends ProtoConnectionHandler> protocolHandler, PacketCallback callback) {
        Protocol.Builder protocol = Protocol.create(namespace, key);
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb
        protocol.addPacket(packetType);
        if (isModernProxy) {
            protocol.setServerAuthHandler(VelocityAuth.class);
            protocol.setClientAuthHandler(VelocityAuth.class);
        }
        if (callback == null) protocol.setServerHandler(protocolHandler);
        else protocol.setServerHandler(protocolHandler, callback);
        protocols.add(protocol.load());
    }
}