package data;

import com.google.gson.annotations.SerializedName;
import java.util.List;


public class Launch {
    private String id;
    private String name;

    @SerializedName("flight_number")
    private int flightNumber;

    @SerializedName("date_utc")
    private String dateUtc;

    private Boolean success;
    private boolean upcoming;
    private String details;


    //список отказов
    private List<Failure> failures;
    //список
    private List<Core> cores;

    //по умолчанию, нужен для  Gson
    public Launch() {

    }


    public Launch(String id, String name, int flightNumber, String dateUtc,
                  Boolean success, boolean upcoming, String details) {
        this.id = id;
        this.name = name;
        this.flightNumber = flightNumber;
        this.dateUtc = dateUtc;
        this.success = success;
        this.upcoming = upcoming;
        this.details = details;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getFlightNumber() {
        return flightNumber;
    }

    public String getDateUtc() {
        return dateUtc;
    }

    public Boolean getSuccess() {
        return success;
    }

    public boolean getUpcoming() {
        return upcoming;
    }

    public String getDetails() {
        return details;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFlightNumber(int flightNumber) {
        this.flightNumber = flightNumber;
    }

    public void setDateUtc(String dateUtc) {
        this.dateUtc = dateUtc;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public void setUpcoming(boolean upcoming) {
        this.upcoming = upcoming;
    }

    public void setDetails(String details) {
        this.details = details;
    }


    public String getSuccessText() {
        //если запуск предстоит в будущем
        if (upcoming) {
            return "предстоит";
        }
        if (success == null) {
            return "неизвестно";
        }
        //если успех то да, если шляпа - нет
        return success ? "да" : "нет";
    }




    //#1  FalconSat | 2006-03-24 | Успех: нет
    //#2  DemoSat   | 2007-03-21 | Успех: нет
    @Override
    public String toString() {
        String shortName = name;

        String shortDate = dateUtc != null && dateUtc.length() >= 10
                ? dateUtc.substring(0, 10) : "N/A";

        //номер запуска, имя выравнивание слева 10, дата, успех/неуспех
        return String.format("#%d  %-10s | %s | Успех: %s",
                flightNumber, shortName, shortDate, getSuccessText());
    }

    //примеры вывода
    //Запуск: Crew-5
    //Номер: 187
    //Дата: 2022-10-05T16:00:18.000Z
    //Успех: да
    //Описание: ...
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Запуск: ").append(name).append("\n");
        sb.append("Номер: ").append(flightNumber).append("\n");
        sb.append("Дата: ").append(dateUtc != null ? dateUtc : "N/A").append("\n");
        sb.append("Успех: ").append(getSuccessText()).append("\n");
        if (details != null && !details.isEmpty()) {
            sb.append("Описание: ").append(details).append("\n");
        }
        return sb.toString();
    }


    public List<Failure> getFailures() {
        return failures;
    }
    public List<Core> getCores() {
        return cores;
    }


    public void setFailures(List<Failure> failures) {
        this.failures = failures;
    }

    public void setCores(List<Core> cores) {
        this.cores = cores;
    }

}
