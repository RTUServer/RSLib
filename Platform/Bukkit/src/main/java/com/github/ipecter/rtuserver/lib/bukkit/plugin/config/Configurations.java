package com.github.ipecter.rtuserver.lib.bukkit.plugin.config;

import com.github.ipecter.rtuserver.lib.bukkit.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.storage.Storage;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.storage.StorageType;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.storage.config.*;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.storage.impl.*;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.util.format.ComponentFormatter;
import com.github.ipecter.rtuserver.lib.bukkit.plugin.util.external.FileUtil;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Configurations {

    private final RSPlugin plugin;

    private final SettingConfiguration setting;
    private final List<String> list = new ArrayList<>();
    private MessageConfiguration message;
    private CommandConfiguration command;
    private JsonConfig json;
    private MariaDBConfig mariadb;
    private MongoDBConfig mongodb;
    private MySQLConfig mysql;
    private PostgreSQLConfig postgresql;
    private SQLiteConfig sqlite;
    private boolean useStorage;
    private boolean isUpdated;

    public Configurations(RSPlugin plugin) {
        this.plugin = plugin;
        setting = new SettingConfiguration(plugin);
        message = new MessageConfiguration(plugin, setting.getLocale());
        command = new CommandConfiguration(plugin, setting.getLocale());
    }

    public void initStorage(String... list) {
        this.list.clear();
        this.list.addAll(List.of(list));
        isUpdated = true;
        storage();
    }

    public void initStorage(List<String> list) {
        this.list.clear();
        this.list.addAll(list);
        isUpdated = true;
        storage();
    }

    private void storage() {
        this.useStorage = true;
        if (!list.isEmpty()) {
            json = new JsonConfig(plugin);
            mongodb = new MongoDBConfig(plugin);
            sqlite = new SQLiteConfig(plugin);
            mysql = new MySQLConfig(plugin);
        }
        loadStorage();
    }

    public void reload() {
        final String locale = setting.getLocale();
        setting.reload();
        if (locale.equalsIgnoreCase(setting.getLocale())) {
            message.reload();
            command.reload();
        } else {
            message = new MessageConfiguration(plugin, setting.getLocale());
            command = new CommandConfiguration(plugin, setting.getLocale());
        }

        if (json != null) json.reload();
        if (mariadb != null) mariadb.reload();
        if (mongodb != null) mongodb.reload();
        if (mysql != null) mysql.reload();
        if (postgresql != null) postgresql.reload();
        if (sqlite != null) sqlite.reload();
        if (useStorage) loadStorage();
    }

    private void loadStorage() {
        StorageType type = setting.getStorage();
        Storage storage = plugin.getStorage();
        switch (type) {
            case JSON -> {
                if (!(storage instanceof Json) || json.isChanged() || isUpdated) {
                    for (String name : list) FileUtil.createFile(plugin.getDataFolder() + "/Data", name + ".json");
                    File[] files = FileUtil.createFolder(plugin.getDataFolder() + "/Data").listFiles();
                    assert files != null;
                    if (storage != null) storage.close();
                    plugin.setStorage(new Json(plugin, files));
                    plugin.console(ComponentFormatter.mini("Storage: Json"));
                }
            }
            case MARIADB -> {
                if (!(storage instanceof MariaDB) || mariadb.isChanged() || isUpdated) {
                    if (storage != null) storage.close();
                    plugin.setStorage(new MariaDB(plugin, list));
                    plugin.console(ComponentFormatter.mini("Storage: MariaDB"));
                }
            }
            case MONGODB -> {
                if (!(storage instanceof MongoDB) || mongodb.isChanged() || isUpdated) {
                    if (storage != null) storage.close();
                    plugin.setStorage(new MongoDB(plugin));
                    plugin.console(ComponentFormatter.mini("Storage: MongoDB"));
                }
            }
            case MYSQL -> {
                if (!(storage instanceof MySQL) || mysql.isChanged() || isUpdated) {
                    if (storage != null) storage.close();
                    plugin.setStorage(new MySQL(plugin, list));
                    plugin.console(ComponentFormatter.mini("Storage: MySQL"));
                }
            }
            case POSTGRESQL -> {
                if (!(storage instanceof PostgreSQL) || postgresql.isChanged() || isUpdated) {
                    if (storage != null) storage.close();
                    plugin.setStorage(new PostgreSQL(plugin));
                    plugin.console(ComponentFormatter.mini("Storage: PostgreSQL"));
                }
            }
            case SQLITE -> {
                if (!(storage instanceof SQLite) || sqlite.isChanged() || isUpdated) {
                    if (storage != null) storage.close();
                    plugin.setStorage(new SQLite(plugin, list));
                    plugin.console(ComponentFormatter.mini("Storage: SQLite"));
                }
            }
        }
        isUpdated = false;
    }
}
