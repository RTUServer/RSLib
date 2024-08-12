package com.github.ipecter.rtuserver.lib.bukkit.util.support;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.github.ipecter.rtuserver.lib.bukkit.RSLib;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DependencyUtil {

    @Getter
    private static Economy economy;
    @Getter
    private static LuckPerms luckPerms;
    @Getter
    private static ProtocolManager protocol;

    public static void checkDependencies(RSLib plugin) {
        ServicesManager manager = Bukkit.getServicesManager();
        if (plugin.isEnabledDependency("Vault")) {
            RegisteredServiceProvider<Economy> provider = manager.getRegistration(Economy.class);
            if (provider != null) economy = provider.getProvider();
        }
        if (plugin.isEnabledDependency("LuckPerms")) {
            RegisteredServiceProvider<LuckPerms> provider = manager.getRegistration(LuckPerms.class);
            if (provider != null) luckPerms = provider.getProvider();
        }
        if (plugin.isEnabledDependency("ProtocolLib")) {
            protocol = ProtocolLibrary.getProtocolManager();
        }
    }
}
