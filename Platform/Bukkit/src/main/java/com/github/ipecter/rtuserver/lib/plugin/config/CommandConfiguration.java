package com.github.ipecter.rtuserver.lib.plugin.config;

import com.github.ipecter.rtuserver.lib.bukkit.RSLib;
import com.github.ipecter.rtuserver.lib.plugin.RSPlugin;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class CommandConfiguration extends RSConfiguration {

    private final Map<String, Object> map = new HashMap<>();

    public CommandConfiguration(RSPlugin plugin, String file) {
        super(plugin, "Translations/Command", "Locale_" + file + ".yml", null);
        setup(this);
    }

    private void init() {
        for (String key : getConfig().getKeys(true)) {
            if (getConfig().isString(key)) {
                String result = getString(key, "");
                if (result.isEmpty()) continue;
                map.put(key, getString(key, "error"));
            }
        }
    }

    public String get(String key) {
        return map.getOrDefault(key, "null").toString();
    }

    public String getCommon(String key) {
        return RSLib.getInstance().getConfigurations().getCommand().get("common." + key);
    }
}
