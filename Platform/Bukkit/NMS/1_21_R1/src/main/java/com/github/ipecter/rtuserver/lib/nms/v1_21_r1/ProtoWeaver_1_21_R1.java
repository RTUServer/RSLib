package com.github.ipecter.rtuserver.lib.nms.v1_21_r1;

import io.netty.channel.Channel;
import io.papermc.paper.configuration.GlobalConfiguration;
import io.papermc.paper.network.ChannelInitializeListener;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import lombok.RequiredArgsConstructor;
import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.protocol.velocity.VelocityAuth;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;
import me.mrnavastar.protoweaver.loader.netty.ProtoDeterminer;
import me.mrnavastar.protoweaver.loader.netty.SSLContext;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.logging.Logger;

@RequiredArgsConstructor
public class ProtoWeaver_1_21_R1 implements ChannelInitializeListener, ProtoLogger.IProtoLogger{

    private final String folder;
    private final Logger logger = Logger.getLogger("ProtoWeaver");

    public void setup() {
        ProtoWeaver.PROTOCOL_LOADED.register(protocol -> {
            ProtoLogger.setLogger(this);
            ChannelInitializeListenerHolder.addListener(Key.key("protoweaver", "internal"), this);
            SSLContext.initKeystore(folder);
            SSLContext.genKeys();
            SSLContext.initContext();


            VelocityAuth.setSecret(GlobalConfiguration.get().proxies.velocity.secret);
        });
    }

    @Override
    public void afterInitChannel(@NonNull Channel channel) {
        ProtoDeterminer.registerToPipeline(channel);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warning(message);
    }

    @Override
    public void error(String message) {
        logger.severe(message);
    }
}
