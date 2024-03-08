package com.github.ipecter.rtuserver.lib.commands;

import com.github.ipecter.rtuserver.lib.RSLib;
import com.github.ipecter.rtuserver.lib.managers.ConfigManager;
import com.github.ipecter.rtuserver.lib.plugin.RSCommand;
import com.github.ipecter.rtuserver.lib.plugin.command.CommandData;
import com.github.ipecter.rtuserver.lib.util.common.ComponentUtil;

import java.util.ArrayList;
import java.util.List;

public class Command extends RSCommand {

    private final ConfigManager config = RSLib.getInstance().getConfigManager();

    public Command() {
        super("rslib");
    }

    @Override
    public void command(CommandData data) {
        if (data.length(1) && data.args(0).equalsIgnoreCase("reload")) {
            if (hasPermission("rslib.reload")) {
                config.init();
                sendMessage(ComponentUtil.systemMessage(getSender(), config.getTranslation("prefix") + config.getTranslation("reload")));
            } else {
                sendMessage(ComponentUtil.systemMessage(getSender(), config.getTranslation("prefix") + config.getTranslation("noPermission")));
            }
        } else wrongUsage();
    }

    private void wrongUsage() {
        sendMessage(ComponentUtil.systemMessage(getSender(), config.getTranslation("prefix") + config.getTranslation("wrongUsage.wrongUsage")));
        sendMessage(ComponentUtil.systemMessage(getSender(), config.getTranslation("wrongUsage.change")));
        if (isOp()) {
            sendMessage(ComponentUtil.systemMessage(getSender(), config.getTranslation("wrongUsage.reload")));
        }
    }

    @Override
    public List<String> tabComplete(CommandData data) {
        if (data.length(1)) {
            List<String> list = new ArrayList<>();
            if (hasPermission("rslib.reload")) {
                list.add("reload");
            }
            return list;
        }
        return List.of();
    }
}
