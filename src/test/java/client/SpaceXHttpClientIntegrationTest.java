package client;

import cashe.CacheManager;
import exceptions.SpaceXApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import testsupport.StubHttps;
import testsupport.TestResourceReader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SpaceXHttpClientIntegrationTest {

    @TempDir
    Path tempDir;

    private String originalUserDir;

    @BeforeAll
    static void installStub() {
        StubHttps.install();
    }

    @BeforeEach
    void setWorkingDir() {
        originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        new CacheManager().clearCache();
        StubHttps.reset();
    }

    @AfterEach
    void restoreWorkingDir() {
        StubHttps.reset();
        System.setProperty("user.dir", originalUserDir);
    }

    @Test
    void getReturnsBodyAndWritesCacheFor200() throws Exception {
        StubHttps.setResponder(request -> new StubHttps.StubResponse(200, "[]"));
        SpaceXHttpClient client = new SpaceXHttpClient();

        String body = client.get("/v5/launches", "all.json");

        assertEquals("[]", body);
        CacheManager manager = new CacheManager();
        assertEquals("[]", manager.readCache("all.json"));
        assertTrue(manager.hasValidCache("all.json"));
    }

    @Test
    void getThrowsForErrorStatusAndIncludesBody() {
        StubHttps.setResponder(request -> new StubHttps.StubResponse(500, "boom"));
        SpaceXHttpClient client = new SpaceXHttpClient();

        SpaceXApiException ex = assertThrows(SpaceXApiException.class,
                () -> client.get("/v5/launches", "error.json"));

        assertEquals(500, ex.getStatusCode());
        assertEquals("boom", ex.getServerAnswer());
    }

    @Test
    void getHandlesNullBodyAsEmptyString() throws Exception {
        StubHttps.setResponder(request -> new StubHttps.StubResponse(200, null));
        SpaceXHttpClient client = new SpaceXHttpClient();

        String body = client.get("/v5/launches", "empty_body.json");

        assertEquals("", body);
        CacheManager manager = new CacheManager();
        assertEquals("", manager.readCache("empty_body.json"));
    }

    @Test
    void postThrowsForErrorStatusWithNullBody() {
        StubHttps.setResponder(request -> new StubHttps.StubResponse(500, null));
        SpaceXHttpClient client = new SpaceXHttpClient();

        SpaceXApiException ex = assertThrows(SpaceXApiException.class,
                () -> client.post("/v5/launches/query", "{}", "error_null.json"));

        assertEquals(500, ex.getStatusCode());
        assertEquals("", ex.getServerAnswer());
    }

    @Test
    void postSendsJsonBodyAndReturnsResponse() throws Exception {
        String requestJson = TestResourceReader.readResource("json/query_success_request.json").trim();
        String responseJson = TestResourceReader.readResource("json/http_docs_empty.json").trim();

        StubHttps.setResponder(request -> {
            String body = new String(request.getBody(), StandardCharsets.UTF_8);
            String contentType = request.getHeaders().get("Content-Type");
            if ("POST".equals(request.getMethod())
                    && "application/json".equals(contentType)
                    && body.contains("success")) {
                return new StubHttps.StubResponse(200, responseJson);
            }
            return new StubHttps.StubResponse(400, "bad request");
        });
        SpaceXHttpClient client = new SpaceXHttpClient();

        String result = client.post("/v5/launches/query", requestJson, "post.json");

        assertTrue(result.contains("docs"));
        CacheManager manager = new CacheManager();
        assertEquals(normalizeJson(responseJson), normalizeJson(manager.readCache("post.json")));
    }

    @Test
    void postThrowsForErrorStatus() {
        StubHttps.setResponder(request -> new StubHttps.StubResponse(400, "bad request"));
        SpaceXHttpClient client = new SpaceXHttpClient();

        SpaceXApiException ex = assertThrows(SpaceXApiException.class,
                () -> client.post("/v5/launches/query", "{}", "post_error.json"));

        assertEquals(400, ex.getStatusCode());
        assertEquals("bad request", ex.getServerAnswer());
    }

    private String normalizeJson(String json) {
        return com.google.gson.JsonParser.parseString(json).toString();
    }
}

