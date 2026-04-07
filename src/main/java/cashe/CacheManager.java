package cashe;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CacheManager {

    private static final String DEFAULT_CACHE_DIR = "cache/";
    private static final long DEFAULT_TTL_MINUTES = 5;  // кеш живёт 5 минут
    private static final DateTimeFormatter CACHE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CacheMetaStorage metaStorage;
    private final File cacheDir;
    private final long ttlMinutes;
    private final Clock clock;

    public CacheManager() {
        this(DEFAULT_CACHE_DIR, DEFAULT_TTL_MINUTES, Clock.systemDefaultZone());
    }

    public CacheManager(String cacheDirPath, long ttlMinutes, Clock clock) {
        this.cacheDir = new File(cacheDirPath);
        this.ttlMinutes = ttlMinutes;
        this.clock = clock;
        this.metaStorage = new CacheMetaStorage(new File(cacheDir, "cache_meta.json").getPath());

        if (!cacheDir.exists()) cacheDir.mkdirs();
    }


    public boolean hasValidCache(String key) {
        File file = cacheFile(key);
        if (!file.exists()) return false;

        LocalDateTime time = metaStorage.getTime(key);
        if (time == null) return false;

        long minutes = Duration.between(time, LocalDateTime.now(clock)).toMinutes();
        return minutes < ttlMinutes;
    }


    public String readCache(String key) {
        File file = cacheFile(key);

        if (!file.exists()) return null;

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
                     )) {

            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            return sb.toString();

        } catch (IOException e) {
            return null;
        }
    }


    public void writeCache(String key, String json) {
        File file = cacheFile(key);

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)
                     )) {

            writer.write(json);

        } catch (IOException e) {
            System.err.println("Ошибка записи кеша: " + e.getMessage());
        }

        metaStorage.updateTime(key, LocalDateTime.now(clock));
    }


    public void clearCache() {
        if (!cacheDir.exists()) return;

        File[] files = cacheDir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isFile()) {
                f.delete();
            }
        }

        metaStorage.clearAll();
    }

    public String getFormattedCacheTime(String key) {
        LocalDateTime time = metaStorage.getTime(key);
        if (time == null) {
            return null;
        }
        return time.format(CACHE_TIME_FORMAT);
    }

    private File cacheFile(String key) {
        return new File(cacheDir, key);
    }
}