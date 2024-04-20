package com.github.ipecter.rtuserver.lib.util.data;

import com.github.ipecter.rtuserver.lib.RSLib;
import com.github.ipecter.rtuserver.lib.util.common.ComponentUtil;
import com.google.common.io.Files;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonStorage implements Storage {

    private final Map<String, JsonFile> map = new HashMap<>();

    public JsonStorage(File[] files, int savePeriod) {
        for (File file : files) {
            try {
                String name = Files.getNameWithoutExtension(file.getName());
                JsonElement json = JsonParser.parseReader(new FileReader(file));
                map.put(name, new JsonFile(file, json != null && !json.isJsonNull() ? json.getAsJsonArray() : new JsonArray(), savePeriod));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean add(String name, JsonObject data) {
        if (!map.containsKey(name)) {
            RSLib.getPlugin().console(ComponentUtil.miniMessage("<red>Can't load " + name + " data!</red>"));
            RSLib.getPlugin().console(ComponentUtil.miniMessage("<red>" + name + " 파일을 불러오는 도중 오류가 발생하였습니다!</red>"));
            return false;
        }
        return map.get(name).add(data);
    }

    @Override
    public boolean set(String name, Pair<String, Object> find, Pair<String, Object> data) {
        if (!map.containsKey(name)) {
            RSLib.getPlugin().console(ComponentUtil.miniMessage("<red>Can't load " + name + " data!</red>"));
            RSLib.getPlugin().console(ComponentUtil.miniMessage("<red>" + name + " 파일을 불러오는 도중 오류가 발생하였습니다!</red>"));
            return false;
        }
        return map.get(name).set(find, data);
    }

    @Override
    public List<JsonObject> get(String name, Pair<String, Object> find) {
        if (!map.containsKey(name)) {
            RSLib.getPlugin().console(ComponentUtil.miniMessage("<red>Can't load " + name + " data!</red>"));
            RSLib.getPlugin().console(ComponentUtil.miniMessage("<red>" + name + " 파일을 불러오는 도중 오류가 발생하였습니다!</red>"));
            return null;
        }
        return map.get(name).get(find);
    }

    public boolean sync(String name) {
        if (!map.containsKey(name)) {
            RSLib.getPlugin().console(ComponentUtil.miniMessage("<red>Can't load " + name + " data!</red>"));
            RSLib.getPlugin().console(ComponentUtil.miniMessage("<red>" + name + " 파일을 불러오는 도중 오류가 발생하였습니다!</red>"));
            return false;
        }
        return map.get(name).sync();
    }

    public void close() {
        for (JsonFile data : map.values()) data.close();
        map.clear();
    }

}
