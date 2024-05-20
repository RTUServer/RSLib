package io.papermc.paper.command.subcommands;

import io.papermc.paper.adventure.providers.ClickCallbackProviderImpl;
import io.papermc.paper.command.PaperSubcommand;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CallbackCommand implements PaperSubcommand {
    @Override
    public boolean execute(final CommandSender sender, final String subCommand, final String[] args) {
        if (args.length != 1) {
            return false;
        }

        final UUID id;
        try {
            id = UUID.fromString(args[0]);
        } catch (final IllegalArgumentException ignored) {
            return false;
        }

        ClickCallbackProviderImpl.CALLBACK_MANAGER.runCallback(sender, id);
        return true;
    }

    @Override
    public boolean tabCompletes() {
        return false;
    }
}
