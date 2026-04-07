package cashe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CacheMetaStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void loadMetaReturnsEmptyMapWhenFileMissing() {
        CacheMetaStorage storage = new CacheMetaStorage(tempDir.resolve("meta.json").toString());

        assertTrue(storage.loadMeta().isEmpty());
    }

    @Test
    void saveAndLoadMetaRoundTrip() {
        CacheMetaStorage storage = new CacheMetaStorage(tempDir.resolve("meta.json").toString());
        Map<String, String> meta = new HashMap<>();
        meta.put("a.json", "2026-01-01T00:00:00");

        storage.saveMeta(meta);

        assertEquals("2026-01-01T00:00:00", storage.loadMeta().get("a.json"));
    }

    @Test
    void updateTimeAndGetTimeWork() {
        CacheMetaStorage storage = new CacheMetaStorage(tempDir.resolve("meta.json").toString());
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);

        storage.updateTime("k.json", now);

        assertEquals(now, storage.getTime("k.json"));
    }

    @Test
    void getTimeReturnsNullForInvalidDateFormat() {
        CacheMetaStorage storage = new CacheMetaStorage(tempDir.resolve("meta.json").toString());
        Map<String, String> meta = new HashMap<>();
        meta.put("k.json", "not-a-date");
        storage.saveMeta(meta);

        assertNull(storage.getTime("k.json"));
    }

    @Test
    void removeKeyAndClearAllWork() {
        CacheMetaStorage storage = new CacheMetaStorage(tempDir.resolve("meta.json").toString());
        storage.updateTime("one.json", LocalDateTime.of(2026, 1, 1, 10, 0));
        storage.updateTime("two.json", LocalDateTime.of(2026, 1, 1, 11, 0));

        storage.removeKey("one.json");
        assertNull(storage.getTime("one.json"));
        assertNotNull(storage.getTime("two.json"));

        storage.clearAll();
        assertTrue(storage.loadMeta().isEmpty());
    }

    @Test
    void loadMetaReturnsEmptyMapForMalformedJson() throws IOException {
        Path file = tempDir.resolve("broken_meta.json");
        Files.writeString(file, "{ not-json ");
        CacheMetaStorage storage = new CacheMetaStorage(file.toString());

        assertTrue(storage.loadMeta().isEmpty());
    }

    @Test
    void updateTimeWithoutExplicitDateStoresCurrentTime() {
        CacheMetaStorage storage = new CacheMetaStorage(tempDir.resolve("meta_now.json").toString());

        storage.updateTime("now.json");

        assertNotNull(storage.getTime("now.json"));
    }

    @Test
    void removeKeyForMissingEntryDoesNothing() {
        CacheMetaStorage storage = new CacheMetaStorage(tempDir.resolve("meta_missing_remove.json").toString());
        storage.updateTime("keep.json", LocalDateTime.of(2026, 1, 1, 11, 0));

        storage.removeKey("absent.json");

        assertNotNull(storage.getTime("keep.json"));
    }
}

