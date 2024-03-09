package com.github.ipecter.rtuserver.lib.plugin;

import com.github.ipecter.rtuserver.lib.plugin.command.CommandData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@RequiredArgsConstructor
public abstract class RSCommand implements CommandExecutor, TabCompleter {

    private final RSPlugin plugin = RSPlugin.getPlugin();
    private final String name;
    private CommandSender sender;
    private Audience audience;

    public boolean isOp() {
        return sender.isOp();
    }

    public boolean hasPermission(String node) {
        return sender.hasPermission(node);
    }

    public void sendMessage(String minimessage) {
        sendMessage(MiniMessage.miniMessage().deserialize(minimessage));
    }

    public void sendMessage(Component component) {
        audience.sendMessage(component);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        this.sender = sender;
        this.audience = RSPlugin.getPlugin().getAdventure().sender(sender);
        command(new CommandData(args));
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        this.sender = sender;
        this.audience = RSPlugin.getPlugin().getAdventure().sender(sender);
        return tabComplete(new CommandData(args));
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
