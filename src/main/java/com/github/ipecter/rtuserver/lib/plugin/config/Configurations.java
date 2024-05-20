package com.github.ipecter.rtuserver.lib.plugin.config;

import com.github.ipecter.rtuserver.lib.plugin.RSPlugin;
import com.github.ipecter.rtuserver.lib.plugin.storage.Storage;
import com.github.ipecter.rtuserver.lib.plugin.storage.StorageType;
import com.github.ipecter.rtuserver.lib.plugin.storage.config.*;
import com.github.ipecter.rtuserver.lib.plugin.storage.impl.*;
import com.github.ipecter.rtuserver.lib.util.common.ComponentUtil;
import com.github.ipecter.rtuserver.lib.util.common.FileUtil;
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

    public Configurations(RSPlugin plugin) {
        this.plugin = plugin;
        setting = new SettingConfiguration(plugin);
        message = new MessageConfiguration(plugin, setting.getLocale());
        command = new CommandConfiguration(plugin, setting.getLocale());
    }

    public void initStorage(String... list) {
        if (useStorage || list == null || list.length == 0) return;
        this.list.addAll(List.of(list));
        storage();
    }

    public void initStorage(List<String> list) {
        if (useStorage || list == null || list.isEmpty()) return;
        this.list.addAll(list);
        storage();
    }

    private void storage() {
        this.useStorage = true;
        json = new JsonConfig(plugin);
        mongodb = new MongoDBConfig(plugin);
        sqlite = new SQLiteConfig(plugin);
        mysql = new MySQLConfig(plugin);
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
                if (!(storage instanceof Json) || json.isChanged()) {
                    for (String name : list) FileUtil.getResource(plugin.getDataFolder() + "/Data", name + ".json");
                    File[] files = FileUtil.getResourceFolder(plugin.getDataFolder() + "/Data").listFiles();
                    assert files != null;
                    if (storage != null) storage.close();
                    plugin.setStorage(new Json(plugin, files));
                    plugin.console(ComponentUtil.miniMessage("Storage: Json"));
                }
            }
            case MARIADB -> {
                if (!(storage instanceof MariaDB) || mariadb.isChanged()) {
                    if (storage != null) storage.close();
                    plugin.setStorage(new MariaDB(plugin));
                    plugin.console(ComponentUtil.miniMessage("Storage: MariaDB"));
                }
            }
            case MONGODB -> {
                if (!(storage instanceof MongoDB) || mongodb.isChanged()) {
                    if (storage != null) storage.close();
                    plugin.setStorage(new MongoDB(plugin));
                    plugin.console(ComponentUtil.miniMessage("Storage: MongoDB"));
                }
            }
            case MYSQL -> {
                if (!(storage instanceof MySQL) || mysql.isChanged()) {
                    if (storage != null) storage.close();
                    plugin.setStorage(new MySQL(plugin, list));
                    plugin.console(ComponentUtil.miniMessage("Storage: MySQL"));
                }
            }
            case POSTGRESQL -> {
                if (!(storage instanceof PostgreSQL) || postgresql.isChanged()) {
                    if (storage != null) storage.close();
                    plugin.setStorage(new PostgreSQL(plugin));
                    plugin.console(ComponentUtil.miniMessage("Storage: PostgreSQL"));
                }
            }
            case SQLITE -> {
                if (!(storage instanceof SQLite) || sqlite.isChanged()) {
                    if (storage != null) storage.close();
                    plugin.setStorage(new SQLite(plugin));
                    plugin.console(ComponentUtil.miniMessage("Storage: SQLite"));
                }
            }
        }
    }
}
