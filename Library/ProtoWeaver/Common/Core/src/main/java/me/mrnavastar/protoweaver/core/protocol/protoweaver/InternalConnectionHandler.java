package me.mrnavastar.protoweaver.core.protocol.protoweaver;

import lombok.Getter;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.netty.Sender;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.internal.CustomPacket;
import me.mrnavastar.protoweaver.api.protocol.internal.ProtocolRegister;
import me.mrnavastar.protoweaver.api.util.ProtoLogger;

public class InternalConnectionHandler {

    @Getter
    protected static final Protocol protocol = Protocol.create("rslib", "protoweaver")
            .setServerHandler(ServerConnectionHandler.class)
            .setClientHandler(ClientConnectionHandler.class)
            .addPacket(AuthStatus.class)
            .addPacket(ProtocolStatus.class)
            .addPacket(CustomPacket.class)
            .addPacket(ProtocolRegister.class)
            .load();

    protected void disconnectIfNeverUpgraded(ProtoConnection connection, Sender sender) {
        if (!connection.getProtocol().toString().equals(protocol.toString())) return;
        if (sender != null) {
            sender.disconnect();
            return;
        }
        connection.disconnect();
    }

    protected void disconnectIfNeverUpgraded(ProtoConnection connection) {
        disconnectIfNeverUpgraded(connection, null);
    }

    protected void protocolNotLoaded(ProtoConnection connection, String name) {
        ProtoLogger.warn("Protocol: " + name + " is not loaded! Closing connection!");
        Sender sender = connection.send(new ProtocolStatus(connection.getProtocol().toString(), name, new byte[]{}, ProtocolStatus.Status.MISSING));
        disconnectIfNeverUpgraded(connection, sender);
    }
}