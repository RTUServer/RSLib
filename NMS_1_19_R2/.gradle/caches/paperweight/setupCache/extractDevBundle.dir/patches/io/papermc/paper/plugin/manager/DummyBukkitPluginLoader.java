package io.papermc.paper.plugin.manager;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.UnknownDependencyException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A purely internal type that implements the now deprecated {@link PluginLoader} after the implementation
 * of papers new plugin system.
 */
@ApiStatus.Internal
public class DummyBukkitPluginLoader implements PluginLoader {

    @Override
    public @NotNull Plugin loadPlugin(@NotNull File file) throws InvalidPluginException, UnknownDependencyException {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull PluginDescriptionFile getPluginDescription(@NotNull File file) throws InvalidDescriptionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Pattern[] getPluginFileFilters() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(@NotNull Listener listener, @NotNull Plugin plugin) {
        return PaperPluginManagerImpl.getInstance().paperEventManager.createRegisteredListeners(listener, plugin);
    }

    @Override
    public void enablePlugin(@NotNull Plugin plugin) {
        Bukkit.getPluginManager().enablePlugin(plugin);
    }

    @Override
    public void disablePlugin(@NotNull Plugin plugin) {
        Bukkit.getPluginManager().disablePlugin(plugin);
    }
}
