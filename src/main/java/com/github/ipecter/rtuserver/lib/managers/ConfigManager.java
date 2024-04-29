package com.github.ipecter.rtuserver.lib.managers;

import com.github.ipecter.rtuserver.lib.RSLib;
import com.github.ipecter.rtuserver.lib.managers.config.CommandConfig;
import com.github.ipecter.rtuserver.lib.managers.config.SettingConfig;
import com.github.ipecter.rtuserver.lib.managers.config.SystemMessageConfig;
import com.github.ipecter.rtuserver.lib.util.common.FileUtil;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    @Getter
    private final SettingConfig setting = new SettingConfig();

    @Getter
    private final SystemMessageConfig systemMessage = new SystemMessageConfig();
    @Getter
    private final CommandConfig command = new CommandConfig();

    private final Map<String, String> msgKeyMap = Collections.synchronizedMap(new HashMap<>());

    public void init() {
        initSetting(FileUtil.copyResource(RSLib.getInstance(), "Setting.yml"));
        initMessage(FileUtil.copyResource(RSLib.getInstance(), "Translations", "Locale_" + setting.getLocale() + ".yml"));
        initModule();
    }

    private void initSetting(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        setting.setEnablePlugin(config.getBoolean("enablePlugin", setting.isEnablePlugin()));
        setting.setMotd(config.getBoolean("motd", setting.isMotd()));
        setting.setLocale(config.getString("locale", setting.getLocale()));
    }

    private void initMessage(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        msgKeyMap.clear();
        for (String key : config.getKeys(true)) {
            if (key.equals("prefix")) {
                String prefixText = config.getString("prefix", "");
                msgKeyMap.put(key, prefixText.isEmpty() ? MiniMessage.miniMessage().serialize(RSLib.getInstance().getPrefix()) : prefixText);
            } else {
                msgKeyMap.put(key, config.getString(key));
            }
        }
        FileUtil.copyResource(RSLib.getInstance(), "Translations", "Locale_EN.yml");
        FileUtil.copyResource(RSLib.getInstance(), "Translations", "Locale_KR.yml");
    }

    public String getTranslation(String key) {
        return msgKeyMap.getOrDefault(key, "");
    }

    private void initModule() {
        initSystemMessage(FileUtil.copyResource(RSLib.getInstance(), "Modules", "SystemMessage.yml"));
        initCommand(FileUtil.copyResource(RSLib.getInstance(), "Modules", "Command.yml"));
    }

    private void initSystemMessage(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        systemMessage.setPrefix(config.getString("prefix", systemMessage.getPrefix()));
        systemMessage.setLore(config.getString("lore", systemMessage.getLore()));
    }

    private void initCommand(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        command.setCooldown(config.getInt("cooldown", command.getCooldown()));
    }

}
