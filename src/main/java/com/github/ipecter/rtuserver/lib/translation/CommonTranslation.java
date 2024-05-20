package com.github.ipecter.rtuserver.lib.translation;

import com.github.ipecter.rtuserver.lib.RSLib;
import com.github.ipecter.rtuserver.lib.plugin.RSPlugin;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class CommonTranslation {

    private final RSPlugin plugin = RSLib.getInstance();
    private final Map<String, String> command = new HashMap<>();
    private final Map<String, String> message = new HashMap<>();

    public String getCommand(String key) {
        return plugin.getConfigurations().getCommand().getTranslation("common." + key);
    }

    public String getMessage(String key) {
        return plugin.getConfigurations().getMessage().getTranslation("common." + key);
    }
}
