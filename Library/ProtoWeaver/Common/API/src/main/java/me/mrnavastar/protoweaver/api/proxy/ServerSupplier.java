package me.mrnavastar.protoweaver.api.proxy;

import java.net.SocketAddress;
import java.util.List;

public interface ServerSupplier {

    List<SocketAddress> getServers();
}