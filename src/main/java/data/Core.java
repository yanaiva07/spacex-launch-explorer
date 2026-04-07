package data;


import com.google.gson.annotations.SerializedName;

public class Core {

    private String core;
    private Integer flight;
    private Boolean reused;

    @SerializedName("landing_success")
    private Boolean landingSuccess;

    public String getCore() {
        return core;
    }
    public Integer getFlight() {
        return flight;
    }
    public Boolean getReused() {
        return reused;
    }
    public Boolean getLandingSuccess() {
        return landingSuccess;
    }
}