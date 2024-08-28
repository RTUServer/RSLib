package com.github.ipecter.rtuserver.lib.bungee;

import lombok.extern.slf4j.Slf4j;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.impl.PacketCallback;
import me.mrnavastar.protoweaver.impl.bungee.BungeeProtoWeaver;
import net.md_5.bungee.api.plugin.Plugin;

@Slf4j(topic = "RSLib")
public class RSLib extends Plugin {

    private final PacketCallback callable = new PacketCallback(this::onPacket);
    private BungeeProtoWeaver protoWeaver;

    @Override
    public void onEnable() {
        log.info("RSLib Bungee loaded.");
        protoWeaver = new BungeeProtoWeaver(callable, getProxy(), getDataFolder().toPath());
        getProxy().getPluginManager().registerListener(this, protoWeaver);
        protoWeaver.onProxyInitialize();
    }

    @Override
    public void onDisable() {
        protoWeaver.disable();
    }


    private void onPacket(ProtoConnection connection, Object object) {

    }
}
