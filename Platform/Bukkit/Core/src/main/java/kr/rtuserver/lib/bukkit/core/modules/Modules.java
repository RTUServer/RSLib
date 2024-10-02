package kr.rtuserver.lib.bukkit.core.modules;

import kr.rtuserver.lib.bukkit.api.RSPlugin;
import lombok.Getter;

@Getter
public class Modules implements kr.rtuserver.lib.bukkit.api.core.modules.Modules {

    private final CommandModule commandModule;
    private final ThemeModule themeModule;

    public Modules(RSPlugin plugin) {
        commandModule = new CommandModule(plugin);
        themeModule = new ThemeModule(plugin);
    }

    public void reload() {
        commandModule.reload();
        themeModule.reload();
    }
}
