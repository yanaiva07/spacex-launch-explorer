package builder;

import com.google.gson.JsonObject;
import data.Launch;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonBuilderTest {

    private final JsonBuilder builder = new JsonBuilder();

    @Test
    void toJsonSerializesSingleLaunch() {
        Launch launch = new Launch("id-1", "Test", 99, "2020-01-01T00:00:00.000Z", true, false, "ok");

        String json = builder.toJson(launch);

        assertTrue(json.contains("\"flight_number\":99"));
        assertTrue(json.contains("\"name\":\"Test\""));
    }

    @Test
    void toJsonSerializesLaunchList() {
        List<Launch> launches = Arrays.asList(
                new Launch("1", "A", 1, "2020-01-01T00:00:00.000Z", true, false, "d1"),
                new Launch("2", "B", 2, "2020-01-02T00:00:00.000Z", false, false, "d2")
        );

        String json = builder.toJson(launches);

        assertTrue(json.startsWith("["));
        assertTrue(json.contains("\"flight_number\":1"));
        assertTrue(json.contains("\"flight_number\":2"));
    }

    @Test
    void toJsonSerializesObjectWithNullFields() {
        Launch launch = new Launch("id-null", "NullLaunch", 100, "2020-01-03T00:00:00.000Z", null, false, null);

        String json = builder.toJson(launch);

        assertTrue(json.contains("\"flight_number\":100"));
        assertFalse(json.contains("\"success\""));
    }

    @Test
    void buildDateQueryBuildsExpectedStructure() {
        String json = builder.buildDateQuery("2020-01-01", "2020-12-31");
        JsonObject root = com.google.gson.JsonParser.parseString(json).getAsJsonObject();

        assertEquals("2020-01-01", root.getAsJsonObject("query").getAsJsonObject("date_utc").get("$gte").getAsString());
        assertEquals("2020-12-31", root.getAsJsonObject("query").getAsJsonObject("date_utc").get("$lte").getAsString());
        assertFalse(root.getAsJsonObject("options").get("pagination").getAsBoolean());
        assertEquals("flight_number", root.getAsJsonObject("options").get("sort").getAsString());
        assertEquals("asc", root.getAsJsonObject("options").get("order").getAsString());
    }

    @Test
    void buildSuccessQueryBuildsExpectedStructure() {
        String json = builder.buildSuccessQuery(true);
        JsonObject root = com.google.gson.JsonParser.parseString(json).getAsJsonObject();

        assertTrue(root.getAsJsonObject("query").get("success").getAsBoolean());
        assertFalse(root.getAsJsonObject("query").get("upcoming").getAsBoolean());
        assertFalse(root.getAsJsonObject("options").get("pagination").getAsBoolean());
        assertEquals("flight_number", root.getAsJsonObject("options").get("sort").getAsString());
        assertEquals("asc", root.getAsJsonObject("options").get("order").getAsString());
    }
}

