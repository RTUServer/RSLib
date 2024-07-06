package com.github.ipecter.rtuserver.lib.plugin.config;

import com.github.ipecter.rtuserver.lib.bukkit.RSLib;
import com.github.ipecter.rtuserver.lib.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.util.common.ComponentUtil;
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
        getString("prefix", "<gradient:#00f260:#057eff>Festival » </gradient>");
        for (String key : getConfig().getKeys(true)) {
            if (getConfig().isString(key)) {
                map.put(key, getString(key, ""));
            }
        }
    }

    public Component getPrefix() {
        String prefix = get("prefix");
        if (prefix.isEmpty()) return getPlugin().getPrefix();
        else return ComponentUtil.miniMessage(prefix);
    }

    public String get(String key) {
        return map.getOrDefault(key, "").toString();
    }

    public String getCommon(String key) {
        return RSLib.getInstance().getConfigurations().getMessage().get("common." + key);
    }
}
