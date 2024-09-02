package com.github.ipecter.rtuserver.lib.bukkit.api.storage.impl;

import com.github.ipecter.rtuserver.lib.bukkit.api.RSPlugin;
import com.github.ipecter.rtuserver.lib.bukkit.api.storage.Storage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class PostgreSQL implements Storage {

    private final RSPlugin plugin;

    private final Gson gson = new Gson();

    public PostgreSQL(RSPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean add(String name, JsonObject data) {
        return false;
    }

    @Override
    public boolean set(String name, Pair<String, Object> find, Pair<String, Object> data) {
        return false;
    }

    @Override
    public List<JsonObject> get(String name, Pair<String, Object> find) {
        return null;
    }

    @Override
    public void close() {

    }
}
