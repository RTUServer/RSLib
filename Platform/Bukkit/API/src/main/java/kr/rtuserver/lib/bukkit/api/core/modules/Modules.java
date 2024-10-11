package kr.rtuserver.lib.bukkit.api.core.modules;

public interface Modules {

    CommandModule getCommandModule();

    ThemeModule getThemeModule();

    void reload();
}
