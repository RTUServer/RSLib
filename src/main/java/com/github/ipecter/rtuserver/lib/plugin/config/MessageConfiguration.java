package com.github.ipecter.rtuserver.lib.plugin.config;

import com.github.ipecter.rtuserver.lib.RSLib;
import com.github.ipecter.rtuserver.lib.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.util.common.ComponentUtil;
import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
public class MessageConfiguration extends RSConfiguration {

    private final Map<String, Object> map = new HashMap<>();

    public MessageConfiguration(RSPlugin plugin, String file) {
        super(plugin, "Translations/Message", "Locale_" + file + ".yml", null);
        setup(this);
    }

    private void init() {
        for (String key : getConfig().getKeys(true)) {
            if (getConfig().isString(key)) {
                map.put(key, getString(key, ""));
            }
        }
    }

    public Component getPrefix() {
        String prefix = getTranslation("prefix");
        if (prefix.isEmpty()) return getPlugin().getPrefix();
        else return ComponentUtil.miniMessage(prefix);
    }

    public String getTranslation(String key) {
        if (key.startsWith("common.")) {
            RSPlugin lib = RSLib.getInstance();
            if (lib != getPlugin()) {
                return lib.getConfigurations().getMessage().getTranslation(key);
            }
        }
        return map.getOrDefault(key, "").toString();
    }
}
