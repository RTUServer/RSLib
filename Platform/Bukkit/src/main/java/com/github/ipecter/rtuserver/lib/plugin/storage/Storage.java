package com.github.ipecter.rtuserver.lib.plugin.storage;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface Storage {
    boolean add(String name, JsonObject data);

    boolean set(String name, Pair<String, Object> find, Pair<String, Object> data);

    List<JsonObject> get(String name, Pair<String, Object> find);

    void close();
}
