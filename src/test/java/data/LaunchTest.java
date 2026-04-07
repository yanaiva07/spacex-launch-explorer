package data;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class LaunchTest {

    @Test
    void getSuccessTextHandlesAllStates() {
        Launch launch = new Launch("id", "Mission", 10, "2020-01-01T00:00:00.000Z", true, false, "details");
        assertEquals("да", launch.getSuccessText());

        launch.setSuccess(false);
        assertEquals("нет", launch.getSuccessText());

        launch.setSuccess(null);
        assertEquals("неизвестно", launch.getSuccessText());

        launch.setUpcoming(true);
        assertEquals("предстоит", launch.getSuccessText());
    }

    @Test
    void toStringFormatsLaunchLine() {
        Launch launch = new Launch("id", "Mission", 12, "2020-01-02T03:04:05.000Z", false, false, "boom");

        String line = launch.toString();

        assertTrue(line.contains("#12"));
        assertTrue(line.contains("Mission"));
        assertTrue(line.contains("2020-01-02"));
        assertTrue(line.contains("Успех: нет"));
    }

    @Test
    void toDetailedStringContainsExpectedFields() {
        Launch launch = new Launch("id", "Crew-5", 187, "2022-10-05T16:00:18.000Z", true, false, "Описание");

        String text = launch.toDetailedString();

        assertTrue(text.contains("Запуск: Crew-5"));
        assertTrue(text.contains("Номер: 187"));
        assertTrue(text.contains("Успех: да"));
        assertTrue(text.contains("Описание: Описание"));
    }

    @Test
    void toDetailedStringOmitsDescriptionWhenEmpty() {
        Launch launch = new Launch("id", "Demo", 3, null, null, true, "");

        String text = launch.toDetailedString();

        assertTrue(text.contains("Дата: N/A"));
        assertFalse(text.contains("Описание:"));
    }

    @Test
    void toStringUsesFallbackDateForShortOrNullDate() {
        Launch shortDate = new Launch("id", "Short", 1, "2020-01-01", true, false, "");
        Launch nullDate = new Launch("id", "Null", 2, null, false, false, "");

        String shortText = shortDate.toString();
        String nullText = nullDate.toString();

        assertTrue(shortText.contains("2020-01-01"));
        assertTrue(nullText.contains("N/A"));
    }

    @Test
    void settersAndGettersWorkForNestedCollections() {
        Launch launch = new Launch();

        Failure failure = new Failure();
        Core core = new Core();

        launch.setId("x");
        launch.setName("n");
        launch.setFlightNumber(1);
        launch.setDateUtc("2020-01-01T00:00:00.000Z");
        launch.setSuccess(Boolean.TRUE);
        launch.setUpcoming(false);
        launch.setDetails("d");
        launch.setFailures(Collections.singletonList(failure));
        launch.setCores(Collections.singletonList(core));

        assertEquals("x", launch.getId());
        assertEquals("n", launch.getName());
        assertEquals(1, launch.getFlightNumber());
        assertEquals("2020-01-01T00:00:00.000Z", launch.getDateUtc());
        assertTrue(launch.getSuccess());
        assertFalse(launch.getUpcoming());
        assertEquals("d", launch.getDetails());
        assertEquals(1, launch.getFailures().size());
        assertEquals(1, launch.getCores().size());
    }
}

