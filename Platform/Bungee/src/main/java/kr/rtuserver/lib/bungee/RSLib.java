package kr.rtuserver.lib.bungee;

import lombok.extern.slf4j.Slf4j;
import me.mrnavastar.protoweaver.impl.bungee.api.BungeeProtoWeaver;
import net.md_5.bungee.api.plugin.Plugin;

@Slf4j(topic = "RSLib")
public class RSLib extends Plugin {

    private BungeeProtoWeaver protoWeaver;

    @Override
    public void onEnable() {
        log.info("RSLib Bungee loaded.");
        protoWeaver = new me.mrnavastar.protoweaver.impl.bungee.core.BungeeProtoWeaver(getProxy(), getDataFolder().toPath());
        getProxy().getPluginManager().registerListener(this, protoWeaver);
    }

    @Override
    public void onDisable() {
        protoWeaver.disable();
    }
}
