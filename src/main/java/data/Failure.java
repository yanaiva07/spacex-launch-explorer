package data;


import java.util.List;

public class Failure {
    //время от старта, когда произошел отказ
    private Integer time;

    //высота в метрах, на которой произошел отказ
    private Integer altitude;

    //описание причины отказа
    private String reason;

    public Integer getTime() {
        return time;
    }
    public Integer getAltitude() {
        return altitude;
    }
    public String getReason() {
        return reason;
    }

}