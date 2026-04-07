package org.example;

import cashe.CacheManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import testsupport.StubHttps;
import testsupport.TestResourceReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MainLoopTest {

    @TempDir
    Path tempDir;

    private String originalUserDir;
    private Path classesDir;

    @BeforeAll
    static void installStub() {
        StubHttps.install();
    }

    @BeforeEach
    void setWorkingDir() {
        originalUserDir = System.getProperty("user.dir");
        classesDir = Path.of(originalUserDir, "target", "classes");
        System.setProperty("user.dir", tempDir.toString());
        new CacheManager().clearCache();
        StubHttps.reset();
    }

    @AfterEach
    void restoreWorkingDir() {
        System.setProperty("user.dir", originalUserDir);
        StubHttps.reset();
    }

    @Test
    void mainHandlesInvalidChoiceAndExits() throws Exception {
        String output = runMain("9\n\n7\n");

        assertTrue(output.contains("Неверный выбор"));
        assertTrue(output.contains("До свидания"));
    }

    @Test
    void mainHandlesSearchByDateInvalidFormat() throws Exception {
        String output = runMain("3\n2020-99-99\n2020-01-01\n\n7\n");

        assertTrue(output.contains("Неверная дата"));
        assertTrue(output.contains("До свидания"));
    }

    @Test
    void mainHandlesSearchByDateRangeError() throws Exception {
        String output = runMain("3\n2021-01-01\n2020-01-01\n\n7\n");

        assertTrue(output.contains("Неверный диапазон дат"));
        assertTrue(output.contains("До свидания"));
    }

    @Test
    void mainHandlesSearchByDateSuccess() throws Exception {
        String response = TestResourceReader.readResource("json/query_response.json").trim();
        StubHttps.setResponder(request -> new StubHttps.StubResponse(200, response));

        String output = runMain("3\n2020-01-01\n2020-12-31\n\n7\n");

        assertTrue(output.contains("Всего: 1"));
        assertTrue(output.contains("До свидания"));
    }

    @Test
    void mainHandlesApiErrorAndContinues() throws Exception {
        StubHttps.setResponder(request -> new StubHttps.StubResponse(500, "boom"));

        String output = runMain("1\n\n7\n");

        assertTrue(output.contains("Ошибка:"));
        assertTrue(output.contains("До свидания"));
    }

    private String runMain(String input) throws Exception {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();

        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));

        URL[] urls = Arrays.stream(System.getProperty("java.class.path").split(java.io.File.pathSeparator))
                .map(path -> Path.of(path).toUri())
                .map(uri -> {
                    try {
                        return uri.toURL();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(url -> url != null)
                .toArray(URL[]::new);
        try (URLClassLoader loader = new URLClassLoader(urls, null)) {
            Class<?> mainClass = Class.forName("org.example.Main", true, loader);
            Method main = mainClass.getMethod("main", String[].class);
            main.invoke(null, (Object) new String[0]);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
            System.setErr(originalErr);
        }

        return out.toString(StandardCharsets.UTF_8) + err.toString(StandardCharsets.UTF_8);
    }
}



