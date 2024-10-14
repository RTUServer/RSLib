package me.mrnavastar.protoweaver.impl.bukkit.core;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.callback.HandlerCallback;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.netty.Sender;
import me.mrnavastar.protoweaver.api.protocol.CompressionType;
import me.mrnavastar.protoweaver.api.protocol.Packet;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.internal.InternalPacket;
import me.mrnavastar.protoweaver.api.protocol.internal.ProtocolRegister;
import me.mrnavastar.protoweaver.api.protocol.internal.StorageSync;
import me.mrnavastar.protoweaver.api.protocol.velocity.VelocityAuth;
import me.mrnavastar.protoweaver.impl.bukkit.api.BukkitProtoHandler;
import me.mrnavastar.protoweaver.impl.bukkit.api.nms.IProtoWeaver;
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
import java.util.HashSet;
import java.util.List;

@Slf4j(topic = "RSLib/ProtoWeaver")
@Getter
public class BukkitProtoWeaver implements me.mrnavastar.protoweaver.impl.bukkit.api.BukkitProtoWeaver {

    private final IProtoWeaver protoWeaver;
    private final HandlerCallback callback;
    private final boolean isModernProxy;
    private final List<Protocol> protocols = new ArrayList<>();
    private final List<Protocol> unregistered = new ArrayList<>();
    private ProtoConnection connection;

    public BukkitProtoWeaver(String sslFolder, String nmsVersion, HandlerCallback callback) {
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
        this.callback = callback;
        this.isModernProxy = protoWeaver.isModernProxy();
        Protocol.Builder protocol = Protocol.create("rslib", "internal");
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb
        protocol.addPacket(ProtocolRegister.class);
        protocol.addPacket(Packet.class);
        protocol.addPacket(Packet.of(StorageSync.class, true, true));
        if (isModernProxy) {
            protocol.setServerAuthHandler(VelocityAuth.class);
            protocol.setClientAuthHandler(VelocityAuth.class);
        }
        protocol.setServerHandler(BukkitProtoHandler.class, this.callback);
        protocol.load();
    }

    public void sendPacket(InternalPacket packet) {
        connection.send(packet);
    }


    public void onReady(HandlerCallback.Ready data) {
        connection = data.protoConnection();
        List<Protocol> copy = ImmutableList.copyOf(unregistered);
        for (Protocol protocol : copy) {
            ProtocolRegister registry = new ProtocolRegister(protocol.getNamespace(), protocol.getKey(), new HashSet<>(protocol.getPacketMap().values()));
            connection.send(registry);
            unregistered.remove(protocol);
        }
    }

    public void registerProtocol(String namespace, String key, Packet packet, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback) {
        Protocol.Builder protocol = Protocol.create(namespace, key);
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb
        protocol.addPacket(packet);
        if (isModernProxy) {
            protocol.setServerAuthHandler(VelocityAuth.class);
            protocol.setClientAuthHandler(VelocityAuth.class);
        }
        if (callback == null) protocol.setServerHandler(protocolHandler);
        else protocol.setServerHandler(protocolHandler, callback);
        Protocol result = protocol.load();
        protocols.add(result);
        if (connection != null) {
            ProtocolRegister registry = new ProtocolRegister(namespace, key, packet);
            Sender sender = connection.send(registry);
            if (sender.isSuccess()) log.info("New Protocol({}) is connected", namespace + ":" + key);
        } else unregistered.add(result);
    }
}