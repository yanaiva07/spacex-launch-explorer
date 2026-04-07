package cashe;

public class CacheKey {

    public static String forAllLaunches() {
        return "launches_all.json";
    }

    public static String forLatestLaunch() {
        return "launches_latest.json";
    }

    public static String forSuccess(boolean success) {
        return "query_success_" + success + ".json";
    }

    public static String forDateRange(String start, String end) {
        return "query_" + start + "_" + end + ".json";
    }
}