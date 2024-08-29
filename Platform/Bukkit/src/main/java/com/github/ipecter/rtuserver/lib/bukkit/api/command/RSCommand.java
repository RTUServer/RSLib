package com.github.ipecter.rtuserver.lib.bukkit.api.command;

import com.github.ipecter.rtuserver.lib.bukkit.RSLib;
import com.github.ipecter.rtuserver.lib.bukkit.api.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.api.config.CommandConfiguration;
import com.github.ipecter.rtuserver.lib.bukkit.api.config.Configurations;
import com.github.ipecter.rtuserver.lib.bukkit.api.config.MessageConfiguration;
import com.github.ipecter.rtuserver.lib.bukkit.api.shortcut.Message;
import com.github.ipecter.rtuserver.lib.bukkit.api.shortcut.Scheduler;
import com.github.ipecter.rtuserver.lib.bukkit.api.util.format.ComponentFormatter;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public abstract class RSCommand extends Command implements Message, Scheduler {

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

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (sender instanceof Player player) {
            Map<UUID, Integer> cooldownMap = lib.getCommandLimit().getExecuteLimit();
            if (cooldownMap.getOrDefault(player.getUniqueId(), 0) <= 0) cooldownMap.put(player.getUniqueId(), cooldown);
            else {
                announce(lib.getConfigurations().getMessage().get("command.cooldown"));
                return true;
            }
        }
        this.sender = sender;
        this.audience = plugin.getAdventure().sender(sender);
        RSCommandData data = new RSCommandData(args);
        if (isMain) {
            if (data.equals(0, command.getCommon("reload"))) {
                if (hasPermission(plugin.getName() + ".reload")) {
                    plugin.getConfigurations().reload();
                    reload(data);
                    announce(message.getCommon("reload"));
                } else announce(message.getCommon("noPermission"));
                return true;
            }
        }
        if (!execute(data)) {
            announce(message.getCommon("wrongUsage"));
            if (hasPermission(plugin.getName() + ".reload"))
                announce(String.format("<gray> - </gray>/%s %s", getName(), command.getCommon("reload")));
            wrongUsage(data);
        }
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        this.sender = sender;
        this.audience = plugin.getAdventure().sender(sender);
        RSCommandData data = new RSCommandData(args);
        List<String> list = new ArrayList<>(tabComplete(data));
        if (isMain && data.length(1)) {
            list.add(command.getCommon("reload"));
        }
        return list;
    }

    protected abstract boolean execute(RSCommandData data);

    protected abstract List<String> tabComplete(RSCommandData data);

    protected void reload(RSCommandData data) {
    }

    protected void wrongUsage(RSCommandData data) {
    }

    protected void announce(String minimessage) {
        send(sender, message.getPrefix().append(ComponentFormatter.mini(minimessage)));
    }

    protected void announce(Component component) {
        send(sender, message.getPrefix().append(component));
    }
}
