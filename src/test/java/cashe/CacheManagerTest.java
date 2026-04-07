package cashe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import testsupport.TestResourceReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class CacheManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void writeAndReadCacheRoundTrip() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        CacheManager manager = new CacheManager(tempDir.toString(), 5, clock);
        String body = TestResourceReader.readResource("json/cache_hello_world.json").trim();

        manager.writeCache("launches_all.json", body);

        assertEquals(normalizeJson(body), normalizeJson(manager.readCache("launches_all.json")));
        assertTrue(manager.hasValidCache("launches_all.json"));
    }

    @Test
    void readCacheReturnsNullForMissingFile() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        CacheManager manager = new CacheManager(tempDir.toString(), 5, clock);

        assertNull(manager.readCache("missing.json"));
        assertFalse(manager.hasValidCache("missing.json"));
    }

    @Test
    void writeCacheOverwritesExistingFile() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        CacheManager manager = new CacheManager(tempDir.toString(), 5, clock);

        manager.writeCache("key.json", "first");
        manager.writeCache("key.json", "second");

        assertEquals("second", manager.readCache("key.json"));
    }

    @Test
    void hasValidCacheBecomesFalseAfterTtl() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        CacheManager manager = new CacheManager(tempDir.toString(), 5, clock);
        manager.writeCache("ttl.json", "value");

        clock.plusMinutes(6);

        assertFalse(manager.hasValidCache("ttl.json"));
    }

    @Test
    void clearCacheRemovesFilesAndMetadata() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        CacheManager manager = new CacheManager(tempDir.toString(), 5, clock);
        manager.writeCache("to_clear.json", "value");

        manager.clearCache();

        assertNull(manager.readCache("to_clear.json"));
        assertFalse(manager.hasValidCache("to_clear.json"));
    }

    @Test
    void getFormattedCacheTimeReturnsNullForUnknownKey() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        CacheManager manager = new CacheManager(tempDir.toString(), 5, clock);

        assertNull(manager.getFormattedCacheTime("missing.json"));
    }

    @Test
    void getFormattedCacheTimeReturnsFormattedDateForKnownKey() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T12:34:56Z"));
        CacheManager manager = new CacheManager(tempDir.toString(), 5, clock);
        manager.writeCache("known.json", "value");

        String formatted = manager.getFormattedCacheTime("known.json");

        assertNotNull(formatted);
        assertTrue(formatted.contains("2026-01-01"));
    }

    @Test
    void hasValidCacheReturnsFalseWhenFileExistsButMetadataMissing() throws IOException {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        CacheManager manager = new CacheManager(tempDir.toString(), 5, clock);
        Files.writeString(tempDir.resolve("orphan.json"), "{}");

        assertFalse(manager.hasValidCache("orphan.json"));
    }

    @Test
    void readCacheReturnsNullWhenKeyPointsToDirectory() throws IOException {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        CacheManager manager = new CacheManager(tempDir.toString(), 5, clock);
        Files.createDirectory(tempDir.resolve("dir_as_key"));

        assertNull(manager.readCache("dir_as_key"));
    }

    @Test
    void writeCacheHandlesNestedPathWithoutParentDirectory() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        CacheManager manager = new CacheManager(tempDir.toString(), 5, clock);

        PrintStream originalErr = System.err;
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err));
        try {
            manager.writeCache("nested/path.json", "value");
        } finally {
            System.setErr(originalErr);
        }

        assertNull(manager.readCache("nested/path.json"));
        assertFalse(manager.hasValidCache("nested/path.json"));
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant initial) {
            this.instant = initial;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.systemDefault();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        void plusMinutes(long minutes) {
            instant = instant.plusSeconds(minutes * 60);
        }
    }

    private String normalizeJson(String json) {
        return com.google.gson.JsonParser.parseString(json).toString();
    }
}

