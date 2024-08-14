package me.mrnavastar.protoweaver.impl.bukkit.nms;

import me.mrnavastar.protoweaver.core.util.ProtoLogger;

public interface IProtoWeaver extends ProtoLogger.IProtoLogger {
    boolean isModernProxy();
    default boolean isPaper() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
