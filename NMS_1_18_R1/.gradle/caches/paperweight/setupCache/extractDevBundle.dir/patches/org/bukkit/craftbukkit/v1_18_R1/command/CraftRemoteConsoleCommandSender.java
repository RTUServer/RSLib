package org.bukkit.craftbukkit.v1_18_R1.command;

import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.rcon.RconConsoleSource;
import org.bukkit.command.RemoteConsoleCommandSender;

public class CraftRemoteConsoleCommandSender extends ServerCommandSender implements RemoteConsoleCommandSender {

    private final RconConsoleSource listener;

    public CraftRemoteConsoleCommandSender(RconConsoleSource listener) {
        this.listener = listener;
    }

    @Override
    public void sendMessage(String message) {
        this.listener.sendMessage(new TextComponent(message + "\n"), Util.NIL_UUID); // Send a newline after each message, to preserve formatting.
    }

    @Override
    public void sendMessage(String... messages) {
        for (String message : messages) {
            this.sendMessage(message);
        }
    }

    @Override
    public String getName() {
        return "Rcon";
    }

    // Paper start
    @Override
    public net.kyori.adventure.text.Component name() {
        return net.kyori.adventure.text.Component.text(this.getName());
    }
    // Paper end

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean value) {
        throw new UnsupportedOperationException("Cannot change operator status of remote controller.");
    }

    // Paper start
    @Override
    public boolean hasPermission(String name) {
        return com.destroystokyo.paper.PaperConfig.consoleHasAllPermissions || super.hasPermission(name);
    }

    @Override
    public boolean hasPermission(org.bukkit.permissions.Permission perm) {
        return com.destroystokyo.paper.PaperConfig.consoleHasAllPermissions || super.hasPermission(perm);
    }
    // Paper end
}
