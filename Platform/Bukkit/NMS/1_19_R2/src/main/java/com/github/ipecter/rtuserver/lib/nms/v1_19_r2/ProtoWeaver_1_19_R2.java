package com.github.ipecter.rtuserver.lib.nms.v1_19_r2;

import com.github.ipecter.rtuserver.lib.nms.BukkitProtoWeaver;
import io.netty.channel.Channel;
import io.papermc.lib.PaperLib;
import io.papermc.paper.configuration.GlobalConfiguration;
import io.papermc.paper.network.ChannelInitializeListener;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import lombok.Getter;
import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.protocol.velocity.VelocityAuth;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;
import me.mrnavastar.protoweaver.loader.netty.ProtoDeterminer;
import me.mrnavastar.protoweaver.loader.netty.SSLContext;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.logging.Logger;

public class ProtoWeaver_1_19_R2 implements BukkitProtoWeaver {

    @Getter
    private final String folder;
    private final Logger logger = Logger.getLogger("ProtoWeaver");

    public ProtoWeaver_1_19_R2(String folder) {
        this.folder = folder;
        ProtoWeaver.PROTOCOL_LOADED.register(protocol -> {
            ProtoLogger.setLogger(this);
            SSLContext.initKeystore(folder);
            SSLContext.genKeys();
            SSLContext.initContext();
            if (PaperLib.isPaper()) {
                ChannelInitializeListenerHolder.addListener(Key.key("protoweaver", "internal"), new Paper());
                VelocityAuth.setSecret(GlobalConfiguration.get().proxies.velocity.secret);
            }
        });
    }

    static class Paper implements ChannelInitializeListener {
        @Override
        public void afterInitChannel(@NonNull Channel channel) {
            ProtoDeterminer.registerToPipeline(channel);
        }
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
