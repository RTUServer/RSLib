package com.github.ipecter.rtuserver.lib.plugin.command;

import com.github.ipecter.rtuserver.lib.RSLib;
import com.github.ipecter.rtuserver.lib.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.plugin.config.CommandConfiguration;
import com.github.ipecter.rtuserver.lib.plugin.config.Configurations;
import com.github.ipecter.rtuserver.lib.plugin.config.MessageConfiguration;
import com.github.ipecter.rtuserver.lib.util.common.ComponentUtil;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public abstract class RSCommand extends Command implements Listener {

    private final RSLib lib = RSLib.getInstance();
    private final Configurations config = lib.getConfigurations();

    private final RSPlugin plugin;
    private final MessageConfiguration message;
    private final CommandConfiguration command;

    private final String name;
    private final boolean isMain;
    private final int cooldown;
    private CommandSender sender;
    private Audience audience;

    public RSCommand(RSPlugin plugin, @NotNull String name) {
        this(plugin, name, false, RSLib.getInstance().getModules().getCommandModule().getExecuteLimit());
    }

    public RSCommand(RSPlugin plugin, @NotNull List<String> name) {
        this(plugin, name, false, RSLib.getInstance().getModules().getCommandModule().getExecuteLimit());
    }

    public RSCommand(RSPlugin plugin, @NotNull String name, int cooldown) {
        this(plugin, name, false, cooldown);
    }

    public RSCommand(RSPlugin plugin, @NotNull List<String> name, int cooldown) {
        this(plugin, name, false, cooldown);
    }

    public RSCommand(RSPlugin plugin, @NotNull String name, boolean isMain) {
        this(plugin, name, isMain, RSLib.getInstance().getModules().getCommandModule().getExecuteLimit());
    }

    public RSCommand(RSPlugin plugin, @NotNull List<String> name, boolean isMain) {
        this(plugin, name, isMain, RSLib.getInstance().getModules().getCommandModule().getExecuteLimit());
    }

    public RSCommand(RSPlugin plugin, @NotNull String name, boolean isMain, int cooldown) {
        this(plugin, List.of(name), isMain, cooldown);
    }

    public RSCommand(RSPlugin plugin, List<String> names, boolean isMain, int cooldown) {
        super(names.get(0));
        this.plugin = plugin;
        this.message = plugin.getConfigurations().getMessage();
        this.command = plugin.getConfigurations().getCommand();
        this.name = names.get(0);
        if (names.size() > 1) setAliases(names);
        this.isMain = isMain;
        this.cooldown = cooldown;
    }

    public boolean isOp() {
        return sender.isOp();
    }

    public boolean hasPermission(String node) {
        return sender.hasPermission(node);
    }

    public void sendAnnounce(Component component) {
        sendMessage(message.getPrefix().append(component));
    }

    public void sendAnnounce(String miniMessage) {
        sendAnnounce(ComponentUtil.formatted(getSender(), miniMessage));
    }

    public void sendMessage(Component component) {
        audience.sendMessage(component);
    }

    public void sendMessage(String miniMessage) {
        sendMessage(ComponentUtil.formatted(getSender(), miniMessage));
    }

//    @Override
//    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
//        if (sender instanceof Player player) {
//            if (cooldownMap.getOrDefault(player.getUniqueId(), 0) <= 0) cooldownMap.put(player.getUniqueId(), cooldown);
//            else {
//                sendMessage(ComponentUtil.miniMessage(message.getCommon("prefix") + message.getCommon("command.cooldown")));
//                return true;
//            }
//        }
//        this.sender = sender;
//        this.audience = RSPlugin.getPlugin().getAdventure().sender(sender);
//        command(new CommandData(args));
//        return true;
//    }
//
//
//    @Nullable
//    @Override
//    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
//        this.sender = sender;
//        this.audience = RSPlugin.getPlugin().getAdventure().sender(sender);
//        return tabComplete(new CommandData(args));
//    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (sender instanceof Player player) {
            Map<UUID, Integer> cooldownMap = lib.getCommandLimit().getExecuteLimit();
            if (cooldownMap.getOrDefault(player.getUniqueId(), 0) <= 0) cooldownMap.put(player.getUniqueId(), cooldown);
            else {
                sendAnnounce(message.get("command.cooldown"));
                return true;
            }
        }
        this.sender = sender;
        this.audience = plugin.getAdventure().sender(sender);
        CommandData data = new CommandData(args);
        if (isMain) {
            if (data.equals(0, command.getCommon("reload"))) {
                if (hasPermission(plugin.getName() + ".reload")) {
                    plugin.getConfigurations().reload();
                    reload(data);
                    sendAnnounce(message.getCommon("reload"));
                } else sendAnnounce(message.getCommon("noPermission"));
                return true;
            }
        }
        if (!command(data)) {
            sendAnnounce(message.getCommon("wrongUsage"));
            if (hasPermission(plugin.getName() + ".reload"))
                sendMessage(String.format("<gray> - </gray> /%s %s", getName(), command.getCommon("reload")));
            wrongUsage(data);
        }
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        this.sender = sender;
        this.audience = plugin.getAdventure().sender(sender);
        CommandData data = new CommandData(args);
        List<String> list = new ArrayList<>(tabComplete(data));
        if (isMain && data.length(1)) {
            list.add(command.getCommon("reload"));
        }
        return list;
    }

    public abstract boolean command(CommandData command);

    public abstract List<String> tabComplete(CommandData command);

    protected void reload(CommandData command) {
    }

    protected void wrongUsage(CommandData command) {
    }
}
