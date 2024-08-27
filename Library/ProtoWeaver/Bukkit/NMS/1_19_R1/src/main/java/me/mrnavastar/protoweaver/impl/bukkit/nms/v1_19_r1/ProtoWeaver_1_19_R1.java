package me.mrnavastar.protoweaver.impl.bukkit.nms.v1_19_r1;

import io.netty.channel.Channel;
import io.papermc.paper.configuration.GlobalConfiguration;
import io.papermc.paper.network.ChannelInitializeListener;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import lombok.extern.slf4j.Slf4j;
import me.mrnavastar.protoweaver.api.protocol.velocity.VelocityAuth;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;
import me.mrnavastar.protoweaver.impl.bukkit.nms.IProtoWeaver;
import me.mrnavastar.protoweaver.loader.netty.ProtoDeterminer;
import me.mrnavastar.protoweaver.loader.netty.SSLContext;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;

@Slf4j(topic = "RSLib/ProtoWeaver")
public class ProtoWeaver_1_19_R1 implements IProtoWeaver {

    public ProtoWeaver_1_19_R1(String folder) {
        ProtoLogger.setLogger(this);
        SSLContext.initKeystore(folder);
        SSLContext.genKeys();
        SSLContext.initContext();
        if (isModernProxy()) {
            info("Detected modern proxy.");
            ChannelInitializeListenerHolder.addListener(Key.key("protoweaver", "internal"), new Paper());
            VelocityAuth.setSecret(GlobalConfiguration.get().proxies.velocity.secret);
        }
    }

    @Override
    public boolean isModernProxy() {
        if (!isPaper()) return false; //TODO: Fabric, Forge, Arclight 등의 Velocity 지원 확장을 고려해야함
        boolean enabled = GlobalConfiguration.get().proxies.velocity.enabled;
        if (!enabled) return false;
        String secret = GlobalConfiguration.get().proxies.velocity.secret;
        if (secret == null || secret.isEmpty()) return false;
        return true;
    }

    @Override
    public void info(String message) {
        log.info(message);
    }

    @Override
    public void warn(String message) {
        log.warn(message);
    }

    @Override
    public void error(String message) {
        log.error(message);
    }

    static class Paper implements ChannelInitializeListener {
        @Override
        public void afterInitChannel(@NonNull Channel channel) {
            ProtoDeterminer.registerToPipeline(channel);
        }
    }
}
