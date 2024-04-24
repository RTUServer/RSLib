package com.github.ipecter.rtuserver.lib.plugin;

import com.github.ipecter.rtuserver.lib.RSLib;
import com.github.ipecter.rtuserver.lib.managers.ConfigManager;
import com.github.ipecter.rtuserver.lib.plugin.command.CommandData;
import com.github.ipecter.rtuserver.lib.util.common.ComponentUtil;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public abstract class RSCommand implements CommandExecutor, TabCompleter, Runnable, Listener {

    private final RSPlugin plugin = RSPlugin.getPlugin();
    private final ConfigManager config = RSLib.getInstance().getConfigManager();

    private final String name;
    private final int cooldown;
    private CommandSender sender;
    private Audience audience;

    private final Map<UUID, Integer> cooldownMap = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> tabCompleteLengthMap = new HashMap<>();
    private final Map<UUID, List<String>> tabCompleteListMap = new HashMap<>();

    public RSCommand(String name) {
        this(name, RSLib.getInstance().getConfigManager().getCommand().getCooldown());
    }

    public RSCommand(String name, int cooldown) {
        this.name = name;
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

    public void sendMessage(String minimessage) {
        sendMessage(ComponentUtil.miniMessage(minimessage));
    }

    public void sendMessage(Component component) {
        audience.sendMessage(component);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (sender instanceof Player player) {
            tabCompleteLengthMap.remove(player.getUniqueId());
            tabCompleteListMap.remove(player.getUniqueId());
            if (cooldownMap.getOrDefault(player.getUniqueId(), 0) <= 0) cooldownMap.put(player.getUniqueId(), cooldown);
            else {
                sendMessage(ComponentUtil.systemMessage(config.getTranslation("command.cooldown")));
                return true;
            }
        }
        this.sender = sender;
        this.audience = RSPlugin.getPlugin().getAdventure().sender(sender);
        command(new CommandData(args));
        return true;
    }


    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (sender instanceof Player player) {
            UUID uuid = player.getUniqueId();
            if ((args.length != tabCompleteLengthMap.getOrDefault(uuid, -1))) {
                tabCompleteLengthMap.put(uuid, args.length);
                this.sender = sender;
                this.audience = RSPlugin.getPlugin().getAdventure().sender(sender);
                return tabCompleteListMap.put(uuid, tabComplete(new CommandData(args)));
            } else {
                return tabCompleteListMap.get(uuid);
            }
        } else return tabComplete(new CommandData(args));
    }

//    @Override
//    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
//        this.sender = sender;
//        this.audience = RSPlugin.getPlugin().getAdventure().sender(sender);
//        command(new CommandData(args));
//        return true;
//    }
//
//    @Override
//    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
//        this.sender = sender;
//        this.audience = RSPlugin.getPlugin().getAdventure().sender(sender);
//        return tabComplete(new CommandData(args));
//    }

    public abstract void command(CommandData command);

    public abstract List<String> tabComplete(CommandData command);

}
