package data;

import java.util.List;

public class LaunchQueryResponse {
    //массив запусков соответствующих запросу
    private List<Launch> docs;

    public List<Launch> getDocs() {
        return docs;
    }
}