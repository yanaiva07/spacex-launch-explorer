package parser;

import data.Launch;
import exceptions.SpaceXApiException;
import org.junit.jupiter.api.Test;
import testsupport.TestResourceReader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonParserTest {

    private final JsonParser parser = new JsonParser();

    @Test
    void parseLaunchMapsSerializedNameAndNestedObjects() throws Exception {
        String json = TestResourceReader.readResource("json/launch_single.json");

        Launch launch = parser.parseLaunch(json);

        assertEquals(1, launch.getFlightNumber());
        assertEquals("FalconSat", launch.getName());
        assertNotNull(launch.getFailures());
        assertEquals(1, launch.getFailures().size());
        assertNull(launch.getFailures().get(0).getAltitude());
        assertNotNull(launch.getCores());
        assertEquals(1, launch.getCores().size());
        assertNull(launch.getCores().get(0).getLandingSuccess());
    }

    @Test
    void parseLaunchMapsFailureAndCoreFields() throws Exception {
        String json = TestResourceReader.readResource("json/launch_single.json");

        Launch launch = parser.parseLaunch(json);

        assertEquals(33, launch.getFailures().get(0).getTime());
        assertEquals("merlin engine failure", launch.getFailures().get(0).getReason());
        assertEquals("5e9e289df35918033d3b2623", launch.getCores().get(0).getCore());
        assertEquals(1, launch.getCores().get(0).getFlight());
        assertFalse(launch.getCores().get(0).getReused());
    }

    @Test
    void parseLaunchesParsesArrayResponse() throws Exception {
        String json = TestResourceReader.readResource("json/launch_array.json");

        List<Launch> launches = parser.parseLaunches(json);

        assertEquals(2, launches.size());
        assertEquals(2, launches.get(1).getFlightNumber());
    }

    @Test
    void parseLaunchesReturnsEmptyListForEmptyArray() throws Exception {
        String json = TestResourceReader.readResource("json/empty_array.json");

        List<Launch> launches = parser.parseLaunches(json);

        assertTrue(launches.isEmpty());
    }

    @Test
    void parseLaunchesParsesQueryDocsWrapper() throws Exception {
        String json = TestResourceReader.readResource("json/query_response.json");

        List<Launch> launches = parser.parseLaunches(json);

        assertEquals(1, launches.size());
        assertEquals("FalconSat", launches.get(0).getName());
    }

    @Test
    void parseLaunchesHandlesNullValuesInFields() throws Exception {
        String json = TestResourceReader.readResource("json/launch_array.json");

        List<Launch> launches = parser.parseLaunches(json);
        Launch second = launches.get(1);

        assertNull(second.getSuccess());
        assertNull(second.getDetails());
    }

    @Test
    void parseLaunchThrowsForInvalidJson() {
        String invalid = TestResourceReader.readResource("json/invalid_launch.json");
        java.io.PrintStream originalErr = System.err;
        java.io.ByteArrayOutputStream err = new java.io.ByteArrayOutputStream();
        System.setErr(new java.io.PrintStream(err));
        SpaceXApiException ex;
        try {
            ex = assertThrows(SpaceXApiException.class, () -> parser.parseLaunch(invalid));
        } finally {
            System.setErr(originalErr);
        }

        assertTrue(ex.getMessage().contains("Failed to parse JSON"));
    }

    @Test
    void parseLaunchThrowsForEmptyJson() {
        SpaceXApiException ex = assertThrows(SpaceXApiException.class, () -> parser.parseLaunch(""));
        assertTrue(ex.getMessage().contains("Empty or null JSON"));
    }

    @Test
    void parseLaunchesThrowsForInvalidJson() {
        String invalid = TestResourceReader.readResource("json/invalid_launch.json");

        SpaceXApiException ex = assertThrows(SpaceXApiException.class, () -> parser.parseLaunches(invalid));

        assertTrue(ex.getMessage().contains("Failed to parse JSON"));
    }

    @Test
    void parseLaunchesThrowsForEmptyJson() {
        SpaceXApiException ex = assertThrows(SpaceXApiException.class, () -> parser.parseLaunches("   "));
        assertTrue(ex.getMessage().contains("Empty or null JSON"));
    }

    @Test
    void parseLaunchesThrowsForObjectWithoutDocs() {
        String json = TestResourceReader.readResource("json/not_docs_wrapper.json");

        SpaceXApiException ex = assertThrows(SpaceXApiException.class, () -> parser.parseLaunches(json));

        assertTrue(ex.getMessage().contains("Failed to parse JSON"));
    }

    @Test
    void parseLaunchesThrowsWhenDocsIsNull() {
        String json = TestResourceReader.readResource("json/query_docs_null.json");

        SpaceXApiException ex = assertThrows(SpaceXApiException.class, () -> parser.parseLaunches(json));

        assertTrue(ex.getMessage().contains("Failed to parse JSON"));
    }

    @Test
    void parseLaunchesThrowsForJsonPrimitive() {
        String json = TestResourceReader.readResource("json/primitive_number.json");

        SpaceXApiException ex = assertThrows(SpaceXApiException.class, () -> parser.parseLaunches(json));

        assertTrue(ex.getMessage().contains("Failed to parse JSON"));
    }

    @Test
    void parseLaunchReturnsNullForJsonNullLiteral() throws Exception {
        String json = TestResourceReader.readResource("json/json_null_literal.json");

        Launch launch = parser.parseLaunch(json);

        assertNull(launch);
    }
}

