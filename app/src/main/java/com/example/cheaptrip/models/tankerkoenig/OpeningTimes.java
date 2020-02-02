package com.example.cheaptrip.models.tankerkoenig;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class OpeningTimes {

    @SerializedName("openingTimes")
    @Expose
    private List<OpeningTime> openingTimes = new ArrayList<OpeningTime>();

    public List<OpeningTime> getOpeningTimes() {
        return openingTimes;
    }

    public void setOpeningTimes(List<OpeningTime> openingTimes) {
        this.openingTimes = openingTimes;
    }

}
