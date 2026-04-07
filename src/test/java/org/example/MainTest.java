package org.example;

import data.Launch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.io.TempDir;
import testsupport.StubHttps;
import testsupport.TestResourceReader;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MainTest {

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
        new cashe.CacheManager().clearCache();
        StubHttps.reset();
    }

    @AfterEach
    void restoreWorkingDir() {
        System.setProperty("user.dir", originalUserDir);
        StubHttps.reset();
    }

    @Test
    @Order(2)
    void parseDateOrNullParsesValidDateAndReturnsNullForInvalid() throws Exception {
        Class<?> mainClass = Class.forName("org.example.Main");
        Method parseDate = mainClass.getDeclaredMethod("parseDateOrNull", String.class);
        parseDate.setAccessible(true);

        Object valid = parseDate.invoke(null, "2020-01-01");
        Object invalid = parseDate.invoke(null, "2020-99-99");

        assertEquals(LocalDate.of(2020, 1, 1), valid);
        assertNull(invalid);
    }


    @Test
    @Order(3)
    void displayLaunchesPrintsEmptyAndNonEmptyStates() throws Exception {
        Class<?> mainClass = Class.forName("org.example.Main");
        Method display = mainClass.getDeclaredMethod("displayLaunches", List.class);
        display.setAccessible(true);

        Launch launch = new Launch("id", "Mission", 10, "2020-01-01T00:00:00.000Z", true, false, "details");

        PrintStream originalOut = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        try {
            display.invoke(null, new Object[]{null});
            display.invoke(null, List.of());
            display.invoke(null, List.of(launch));
        } finally {
            System.setOut(originalOut);
        }

        String output = out.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Нет запусков, удовлетворяющих критериям."));
        assertTrue(output.contains("#10"));
        assertTrue(output.contains("Всего: 1"));
    }

    @Test
    @Order(5)
    void showAllLaunchesPrintsListAndCount() throws Exception {
        String response = TestResourceReader.readResource("json/launch_array.json").trim();
        StubHttps.setResponder(request -> new StubHttps.StubResponse(200, response));

        Class<?> mainClass = Class.forName("org.example.Main");
        Method showAll = mainClass.getDeclaredMethod("showAllLaunches");
        showAll.setAccessible(true);

        PrintStream originalOut = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        try {
            showAll.invoke(null);
        } finally {
            System.setOut(originalOut);
        }

        String output = out.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("=== Все запуски ==="));
        assertTrue(output.contains("Всего запусков: 2"));
    }

    @Test
    @Order(6)
    void showAllLaunchesHandlesEmptyList() throws Exception {
        String response = TestResourceReader.readResource("json/empty_array.json").trim();
        StubHttps.setResponder(request -> new StubHttps.StubResponse(200, response));

        Class<?> mainClass = Class.forName("org.example.Main");
        Method showAll = mainClass.getDeclaredMethod("showAllLaunches");
        showAll.setAccessible(true);

        PrintStream originalOut = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        try {
            showAll.invoke(null);
        } finally {
            System.setOut(originalOut);
        }

        String output = out.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Не удалось получить список запусков"));
    }

    @Test
    @Order(7)
    void showLatestLaunchPrintsDetails() throws Exception {
        String response = TestResourceReader.readResource("json/launch_single.json").trim();
        StubHttps.setResponder(request -> new StubHttps.StubResponse(200, response));

        Class<?> mainClass = Class.forName("org.example.Main");
        Method showLatest = mainClass.getDeclaredMethod("showLatestLaunch");
        showLatest.setAccessible(true);

        PrintStream originalOut = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        try {
            showLatest.invoke(null);
        } finally {
            System.setOut(originalOut);
        }

        String output = out.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("=== Последний запуск ==="));
        assertTrue(output.contains("Запуск:"));
    }

    @Test
    @Order(8)
    void showSuccessLaunchesPrintsHeaderAndCount() throws Exception {
        String response = TestResourceReader.readResource("json/query_response.json").trim();
        StubHttps.setResponder(request -> new StubHttps.StubResponse(200, response));

        Class<?> mainClass = Class.forName("org.example.Main");
        Method showSuccess = mainClass.getDeclaredMethod("showSuccessLaunches", boolean.class);
        showSuccess.setAccessible(true);

        PrintStream originalOut = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        try {
            showSuccess.invoke(null, true);
        } finally {
            System.setOut(originalOut);
        }

        String output = out.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("=== Успешные запуски ==="));
        assertTrue(output.contains("Всего: 1"));
    }

    @Test
    @Order(9)
    void showFailureLaunchesPrintsHeaderAndCount() throws Exception {
        String response = TestResourceReader.readResource("json/query_response.json").trim();
        StubHttps.setResponder(request -> new StubHttps.StubResponse(200, response));

        Class<?> mainClass = Class.forName("org.example.Main");
        Method showSuccess = mainClass.getDeclaredMethod("showSuccessLaunches", boolean.class);
        showSuccess.setAccessible(true);

        PrintStream originalOut = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        try {
            showSuccess.invoke(null, false);
        } finally {
            System.setOut(originalOut);
        }

        String output = out.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("=== Неудачные запуски ==="));
        assertTrue(output.contains("Всего: 1"));
    }

    @Test
    @Order(4)
    void printMenuOutputsAllOptions() throws Exception {
        Class<?> mainClass = Class.forName("org.example.Main");
        Method printMenu = mainClass.getDeclaredMethod("printMenu");
        printMenu.setAccessible(true);

        PrintStream originalOut = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        try {
            printMenu.invoke(null);
        } finally {
            System.setOut(originalOut);
        }

        String output = out.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("1. Показать все запуски"));
        assertTrue(output.contains("2. Показать последний запуск"));
        assertTrue(output.contains("3. Поиск запусков по дате"));
        assertTrue(output.contains("4. Показать только успешные запуски"));
        assertTrue(output.contains("5. Показать только неудачные запуски"));
        assertTrue(output.contains("6. Очистить кеш"));
        assertTrue(output.contains("7. Выход"));
    }

}


