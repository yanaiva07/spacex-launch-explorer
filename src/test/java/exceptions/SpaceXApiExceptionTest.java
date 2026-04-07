package exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpaceXApiExceptionTest {

    @Test
    void httpConstructorStoresStatusAndBody() {
        SpaceXApiException ex = new SpaceXApiException("HTTP error", 500, "server down");

        assertEquals(500, ex.getStatusCode());
        assertEquals("server down", ex.getServerAnswer());
        assertEquals("HTTP error", ex.getMessage());
    }

    @Test
    void networkConstructorStoresCause() {
        RuntimeException cause = new RuntimeException("network fail");
        SpaceXApiException ex = new SpaceXApiException("Network error", cause);

        assertEquals(-1, ex.getStatusCode());
        assertNull(ex.getServerAnswer());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void messageOnlyConstructorSetsDefaults() {
        SpaceXApiException ex = new SpaceXApiException("Custom error");

        assertEquals(-1, ex.getStatusCode());
        assertNull(ex.getServerAnswer());
        assertEquals("Custom error", ex.getMessage());
    }
}

