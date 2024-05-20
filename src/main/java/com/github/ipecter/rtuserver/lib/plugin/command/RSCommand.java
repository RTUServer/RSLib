package com.github.ipecter.rtuserver.lib.plugin.command;

import com.github.ipecter.rtuserver.lib.RSLib;
import com.github.ipecter.rtuserver.lib.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.plugin.config.CommandConfiguration;
import com.github.ipecter.rtuserver.lib.plugin.config.Configurations;
import com.github.ipecter.rtuserver.lib.plugin.config.MessageConfiguration;
import com.github.ipecter.rtuserver.lib.translation.CommonTranslation;
import com.github.ipecter.rtuserver.lib.util.common.ComponentUtil;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public abstract class RSCommand extends Command implements Runnable, Listener {

    private final RSLib lib = RSLib.getInstance();
    private final Configurations config = lib.getConfigurations();
    private final CommonTranslation translation = RSLib.getInstance().getTranslation();

    private final RSPlugin plugin;
    private final MessageConfiguration message;
    private final CommandConfiguration command;

    private final String name;
    private final CommandType type;
    private final int cooldown;
    private final Map<UUID, Integer> cooldownMap = new ConcurrentHashMap<>();
    private CommandSender sender;
    private Audience audience;

    public RSCommand(RSPlugin plugin, @NotNull String name) {
        this(plugin, name, CommandType.NORMAL, RSLib.getInstance().getModules().getCommandModule().getCooldown());
    }

    public RSCommand(RSPlugin plugin, @NotNull List<String> name) {
        this(plugin, name, CommandType.NORMAL, RSLib.getInstance().getModules().getCommandModule().getCooldown());
    }

    public RSCommand(RSPlugin plugin, @NotNull String name, int cooldown) {
        this(plugin, name, CommandType.NORMAL, cooldown);
    }

    public RSCommand(RSPlugin plugin, @NotNull List<String> name, int cooldown) {
        this(plugin, name, CommandType.NORMAL, cooldown);
    }

    public RSCommand(RSPlugin plugin, @NotNull String name, CommandType type) {
        this(plugin, name, type, RSLib.getInstance().getModules().getCommandModule().getCooldown());
    }

    public RSCommand(RSPlugin plugin, @NotNull List<String> name, CommandType type) {
        this(plugin, name, type, RSLib.getInstance().getModules().getCommandModule().getCooldown());
    }

    public RSCommand(RSPlugin plugin, @NotNull String name, CommandType type, int cooldown) {
        this(plugin, List.of(name), type, cooldown);
    }

    public RSCommand(RSPlugin plugin, List<String> names, CommandType type, int cooldown) {
        super(names.get(0));
        this.plugin = plugin;
        this.message = plugin.getConfigurations().getMessage();
        this.command = plugin.getConfigurations().getCommand();
        this.name = names.get(0);
        if (names.size() > 1) setAliases(names);
        this.type = type;
        this.cooldown = cooldown;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, 0, 1);
    }

    @Override
    public void run() {
        for (UUID uuid : cooldownMap.keySet()) {
            if (cooldownMap.get(uuid) > 0) cooldownMap.put(uuid, cooldownMap.get(uuid) - 1);
            else cooldownMap.remove(uuid);
        }
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
//                sendMessage(ComponentUtil.miniMessage(translation.getMessage("prefix") + translation.getMessage("command.cooldown")));
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
            if (cooldownMap.getOrDefault(player.getUniqueId(), 0) <= 0) cooldownMap.put(player.getUniqueId(), cooldown);
            else {
                sendAnnounce(message.getTranslation("command.cooldown"));
                return true;
            }
        }
        this.sender = sender;
        this.audience = plugin.getAdventure().sender(sender);
        CommandData data = new CommandData(args);
        if (type != CommandType.SINGLE) {
            if (!data.isEmpty()) {
                if (type == CommandType.MAIN) {
                    if (data.equals(0, translation.getCommand("reload"))) {
                        if (hasPermission(plugin.getName() + ".reload")) {
                            plugin.getConfigurations().reload();
                            reload(data);
                            sendAnnounce(translation.getMessage("reload"));
                        } else sendAnnounce(translation.getMessage("noPermission"));
                        return true;
                    }
                }
            } else {
                sendAnnounce(translation.getMessage("wrongUsage"));
                if (hasPermission(plugin.getName() + ".reload"))
                    sendMessage(String.format("<gray> - </gray> /%s %s", getName(), translation.getCommand("reload")));
                wrongUsage(data);
                return true;
            }
        }
        command(data);
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        this.sender = sender;
        this.audience = plugin.getAdventure().sender(sender);
        CommandData data = new CommandData(args);
        List<String> list = new ArrayList<>(tabComplete(data));
        if (type == CommandType.MAIN && data.length(1)) {
            list.add(translation.getCommand("reload"));
        }
        return list;
    }

    public abstract void command(CommandData command);

    public abstract List<String> tabComplete(CommandData command);

    protected void reload(CommandData command) {
    }

    protected void wrongUsage(CommandData command) {
    }
}
