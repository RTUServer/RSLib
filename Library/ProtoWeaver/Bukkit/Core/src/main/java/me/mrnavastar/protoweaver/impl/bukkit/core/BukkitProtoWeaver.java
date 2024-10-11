package me.mrnavastar.protoweaver.impl.bukkit.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.callback.HandlerCallback;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.netty.Sender;
import me.mrnavastar.protoweaver.api.protocol.CompressionType;
import me.mrnavastar.protoweaver.api.protocol.PacketType;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.Request;
import me.mrnavastar.protoweaver.api.protocol.internal.RegisterRequest;
import me.mrnavastar.protoweaver.api.protocol.internal.RegisterResponse;
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j(topic = "RSLib/ProtoWeaver")
@Getter
public class BukkitProtoWeaver implements me.mrnavastar.protoweaver.impl.bukkit.api.BukkitProtoWeaver {

    private final IProtoWeaver protoWeaver;
    private final boolean isModernProxy;
    private final HandlerCallback callable = new HandlerCallback(this::onReady, this::onPacket);
    private final Map<String, Protocol> protocols = new ConcurrentHashMap<>(); // Registered
    private final Map<String, Request> unregistered = new ConcurrentHashMap<>();
    private final Map<String, Request> unrequested = new ConcurrentHashMap<>();

    private ProtoConnection connection;

    public BukkitProtoWeaver(String sslFolder, String nmsVersion) {
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
        Protocol.Builder protocol = Protocol.create("rslib", "internal");
        protocol.setCompression(CompressionType.SNAPPY);
        protocol.setMaxPacketSize(67108864); // 64mb
        protocol.addPacket(Object.class);
        protocol.addPacket(RegisterRequest.class);
        protocol.addPacket(RegisterResponse.class);
        if (isModernProxy) {
            protocol.setServerAuthHandler(VelocityAuth.class);
            protocol.setClientAuthHandler(VelocityAuth.class);
        }
        protocol.setServerHandler(BukkitProtoHandler.class, callable);
        protocol.load();
    }

    private void onReady(HandlerCallback.Ready data) {
        connection = data.protoConnection();
        for (String namespaceKey : unrequested.keySet()) {
            Request request = unrequested.get(namespaceKey);
            RegisterRequest registerRequest = new RegisterRequest(request.namespace(), request.key(), request.packetType(), request.global());
            Sender sender = connection.send(registerRequest);
            if (sender.isSuccess()) {
                unrequested.remove(request.namespaceKey());
                unregistered.put(request.namespaceKey(), request);
            } else log.warn("Failed to request protocol {}", request.namespaceKey());
        }
    }


    public void registerProtocol(String namespace, String key, Class<?> packetType, boolean global, Class<? extends ProtoConnectionHandler> protocolHandler) {
        registerProtocol(namespace, key, packetType, global, protocolHandler, null);
    }

    public void registerProtocol(String namespace, String key, Class<?> packetType, boolean global, Class<? extends ProtoConnectionHandler> protocolHandler, HandlerCallback callback) {
        Request request = new Request(namespace, key, packetType, global, protocolHandler, callback);
        if (connection != null) {
            unregistered.put(request.namespaceKey(), request);
            RegisterRequest registerRequest = new RegisterRequest(namespace, key, packetType, global);
            Sender sender = connection.send(registerRequest);
        } else {
            unrequested.put(request.namespaceKey(), request);
        }
    }

    private void onPacket(HandlerCallback.Packet packet) {
        if (packet.packet() instanceof RegisterResponse response) {
            Request request = unregistered.get(response.getNamespaceKey());
            PacketType type = PacketType.of(response.getClassType(), request.global(), response.getResult().notFound());
            Protocol.Builder protocol = Protocol.create(response.getNamespace(), response.getKey());
            protocol.setCompression(CompressionType.SNAPPY);
            protocol.setMaxPacketSize(67108864); // 64mb
            protocol.addPacket(type);
            if (isModernProxy) {
                protocol.setServerAuthHandler(VelocityAuth.class);
                protocol.setClientAuthHandler(VelocityAuth.class);
            }
            if (request.callback() == null) protocol.setServerHandler(request.protocolHandler());
            else protocol.setServerHandler(request.protocolHandler(), request.callback());
            Protocol result = protocol.load();
            unregistered.remove(response.getNamespaceKey());
            protocols.put(request.namespaceKey(), result);
            if (!response.getResult().alreadyLoaded()) packet.protoConnection().send(response);
        }
    }
}