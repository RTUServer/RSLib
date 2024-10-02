package kr.rtuserver.lib.bukkit.plugin;

import kr.rtuserver.lib.bukkit.api.RSPlugin;
import kr.rtuserver.lib.bukkit.plugin.commands.RSLibCommand;
import kr.rtuserver.lib.common.api.cdi.LightDI;
import lombok.Getter;
import me.mrnavastar.protoweaver.api.callback.PacketCallback;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import org.bukkit.Bukkit;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class RSLib extends RSPlugin {

    @Getter
    private static RSLib instance;
    @Getter
    private final PacketCallback callable = new PacketCallback(this::onPacket);

    public RSLib() {
        super();
        List<String> list = new ArrayList<>();
        list.add(kr.rtuserver.lib.bukkit.core.RSFramework.class.getPackageName());
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin instanceof RSPlugin) {
                list.add(plugin.getClass().getPackageName());
            }
        }
        LightDI.init(list.toArray(new String[list.size()]));
    }

    @Override
    protected void initialize() {
        getFramework().load(this);
    }

    @Override
    protected void load() {
        instance = this;
    }

    @Override
    protected void onPacket(ProtoConnection connection, Object object) {
    }

    @Override
    protected void enable() {
        getFramework().enable(this);

        registerPermission(getName() + ".motd", PermissionDefault.OP);
        registerCommand(new RSLibCommand(this));
    }

    @Override
    protected void disable() {
        getFramework().disable(this);
    }
}
