package cashe;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CacheKeyTest {

    @Test
    void keyFactoriesReturnExpectedNames() {
        assertEquals("launches_all.json", CacheKey.forAllLaunches());
        assertEquals("launches_latest.json", CacheKey.forLatestLaunch());
        assertEquals("query_success_true.json", CacheKey.forSuccess(true));
        assertEquals("query_2020-01-01_2020-12-31.json", CacheKey.forDateRange("2020-01-01", "2020-12-31"));
    }
}

