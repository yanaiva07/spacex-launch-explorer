package cashe;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class CacheMetaStorage {

    private final File metaFile;
    private final Gson gson;


    public CacheMetaStorage(String metaFilePath) {
        this.metaFile = new File(metaFilePath);
        this.gson = new Gson();
    }

    public Map<String, String> loadMeta() {
        if (!metaFile.exists()) return new HashMap<>();

        try (Reader reader = new FileReader(metaFile)) {
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            return gson.fromJson(reader, type);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    public void saveMeta(Map<String, String> meta) {
        File parent = metaFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (Writer writer = new FileWriter(metaFile)) {
            gson.toJson(meta, writer);
        } catch (Exception ignored) {}
    }

    public void updateTime(String key) {
        updateTime(key, LocalDateTime.now());
    }

    public void updateTime(String key, LocalDateTime dateTime) {
        Map<String, String> meta = loadMeta();
        meta.put(key, dateTime.toString());
        saveMeta(meta);
    }

    public LocalDateTime getTime(String key) {
        Map<String, String> meta = loadMeta();
        if (!meta.containsKey(key)) return null;
        try {
            return LocalDateTime.parse(meta.get(key));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public void removeKey(String key) {
        Map<String, String> meta = loadMeta();
        if (meta.remove(key) != null) {
            saveMeta(meta);
        }
    }

    public void clearAll() {
        saveMeta(new HashMap<>());
    }
}