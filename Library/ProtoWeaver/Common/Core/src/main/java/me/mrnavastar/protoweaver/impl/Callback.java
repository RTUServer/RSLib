package me.mrnavastar.protoweaver.impl;

import me.mrnavastar.protoweaver.api.netty.ProtoConnection;

public interface Callback<T> {
    public abstract void run(ProtoConnection connection, T packet);
}
