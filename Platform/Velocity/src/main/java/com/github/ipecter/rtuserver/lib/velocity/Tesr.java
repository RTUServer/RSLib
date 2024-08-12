package com.github.ipecter.rtuserver.lib.velocity;

import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;

public class Tesr implements ProtoConnectionHandler {

    @Override
    public void onReady(ProtoConnection connection) {
        connection.send("hi!!!");
    }
}
