package com.github.ipecter.rtuserver.lib.util.data;

import com.github.ipecter.rtuserver.lib.RSLib;
import com.github.ipecter.rtuserver.lib.util.common.ComponentUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


public class JsonFile {
    private final Gson gson = new Gson();
    private final File file;
    @Getter
    private JsonArray data;
    @Getter
    private final AtomicBoolean needSave = new AtomicBoolean(false);
    private final BukkitTask task;

    public JsonFile(File file, JsonArray data, int savePeriod) {
        this.file = file;
        this.data = data;
        this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(RSLib.getPlugin(), () -> {
            if (!needSave.get()) return;
            Bukkit.getScheduler().runTask(RSLib.getPlugin(), () -> {
                save();
                needSave.set(false);
            });
        }, savePeriod, savePeriod);
    }

    public boolean add(JsonObject value) {
        data.add(value);
        needSave.lazySet(true);
        return true;
    }

    protected boolean set(Pair<String, Object> find, Pair<String, Object> value) {
        Map<Integer, JsonObject> list = find(find);
        if (list.isEmpty()) return false;
        JsonArray copy = data.deepCopy();
        for (int key : list.keySet()) {
            JsonElement resultValue = list.get(key);
            if (resultValue == null || resultValue.isJsonNull()) return false;
            JsonObject valObj = resultValue.getAsJsonObject();
            if (value == null) {
                copy.remove(key);
            } else {
                Object object = value.getValue();
                if (object instanceof JsonElement element) {
                    valObj.add(value.getKey(), element);
                } else if (object instanceof Number number) {
                    valObj.addProperty(value.getKey(), number);
                } else if (object instanceof Boolean bool) {
                    valObj.addProperty(value.getKey(), bool);
                } else if (object instanceof String str) {
                    valObj.addProperty(value.getKey(), str);
                } else {
                    RSLib.getPlugin().console(ComponentUtil.miniMessage("<red>Unsupported type of data tried to be saved! Only supports JsonElement, Number, Boolean, and String</red>"));
                    RSLib.getPlugin().console(ComponentUtil.miniMessage("<red>지원하지 않는 타입의 데이터가 저장되려고 했습니다! JsonElement, Number, Boolean, String만 지원합니다</red>"));
                    return false;
                }
                if (copy.contains(valObj)) {
                    copy.set(key, valObj);
                } else {
                    copy.add(valObj);
                }
            }
        }
        this.data = copy;
        needSave.lazySet(true);
        return true;
    }

    @Nullable
    protected List<JsonObject> get(Pair<String, Object> find) {
        Map<Integer, JsonObject> list = find(find);
        return new ArrayList<>(list.values());
    }

    private Map<Integer, JsonObject> find(Pair<String, Object> find) {
        List<JsonElement> list = data.asList();
        Map<Integer, JsonObject> result = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            JsonObject object = list.get(i).getAsJsonObject();
            JsonElement get = object.get(find.getKey());
            if (get == null || get.isJsonNull()) continue;
            if (!gson.fromJson(get, Object.class).equals(find.getValue())) continue;
            result.put(i, object);
        }
        return result;
    }

    private void save() {
        try (Writer writer = new FileWriter(file)) {
            gson.newBuilder().setPrettyPrinting().create().toJson(data, writer);
        } catch (IOException e) {
            RSLib.getPlugin().console(ComponentUtil.miniMessage("<red>Error when saving " + file.getName() + "!</red>"));
            RSLib.getPlugin().console(ComponentUtil.miniMessage("<red> " + file.getName() + " 파일을 저장하는 도중 오류가 발생하였습니다!</red>"));
        }
    }

    protected void close() {
        task.cancel();
        save();
    }
}
