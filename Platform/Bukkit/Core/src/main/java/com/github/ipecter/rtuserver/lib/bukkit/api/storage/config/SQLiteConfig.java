package com.github.ipecter.rtuserver.lib.bukkit.api.storage.config;

import com.github.ipecter.rtuserver.lib.bukkit.api.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.api.config.RSConfiguration;
import lombok.Getter;

@Getter
public class SQLiteConfig extends RSConfiguration {
    private String file = "./Data/SQLite.sql";
    private String database = "";
    private String username = "";
    private String password = "";
    private String tablePrefix = getPlugin().getName() + "_";

    public SQLiteConfig(RSPlugin plugin) {
        super(plugin, "Configs/Storages", "SQLite.yml", null);
        setup(this);
    }

    private void init() {
        file = getString("file", file);
        database = getString("database", database);
        username = getString("username", username);
        password = getString("password", password);
        tablePrefix = getString("tablePrefix", tablePrefix);
    }

}
