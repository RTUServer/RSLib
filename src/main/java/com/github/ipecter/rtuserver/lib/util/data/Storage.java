package com.github.ipecter.rtuserver.lib.util.data;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.tuple.Pair;

public interface Storage {
    boolean add(String name, JsonObject data);

    boolean set(String name, Pair<String, Object> find, Pair<String, Object> data);

    JsonObject get(String name, Pair<String, Object> find);

    void close();
}
