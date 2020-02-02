package com.example.cheaptrip.models.tankerkoenig;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Period {

    @SerializedName("startp")
    @Expose
    private String startp;
    @SerializedName("endp")
    @Expose
    private String endp;

    public String getStartp() {
        return startp;
    }

    public void setStartp(String startp) {
        this.startp = startp;
    }

    public String getEndp() {
        return endp;
    }

    public void setEndp(String endp) {
        this.endp = endp;
    }

}

