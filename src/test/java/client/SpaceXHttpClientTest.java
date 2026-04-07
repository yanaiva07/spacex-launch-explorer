package client;

import cashe.CacheManager;
import exceptions.SpaceXApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SpaceXHttpClientTest {

    @TempDir
    Path tempDir;

    private String originalUserDir;

    @BeforeEach
    void setWorkingDir() {
        originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        new CacheManager().clearCache();
    }

    @AfterEach
    void restoreWorkingDir() {
        System.setProperty("user.dir", originalUserDir);
    }

    @Test
    void getReturnsCachedBodyWhenCacheIsValid() throws Exception {
        CacheManager manager = new CacheManager();
        manager.writeCache("cached_get.json", "{\"ok\":true}");
        SpaceXHttpClient client = new SpaceXHttpClient();

        String result = client.get("/v5/launches", "cached_get.json");

        assertEquals("{\"ok\":true}", result);
    }

    @Test
    void postReturnsCachedBodyWhenCacheIsValid() throws Exception {
        CacheManager manager = new CacheManager();
        manager.writeCache("cached_post.json", "{\"docs\":[]}");
        SpaceXHttpClient client = new SpaceXHttpClient();

        String result = client.post("/v5/launches/query", "{}", "cached_post.json");

        assertEquals("{\"docs\":[]}", result);
    }
}

