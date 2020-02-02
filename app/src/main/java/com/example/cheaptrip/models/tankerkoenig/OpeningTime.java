package com.example.cheaptrip.models.tankerkoenig;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class OpeningTime {
    @SerializedName("applicable_days")
    @Expose
    private Integer applicableDays;
    @SerializedName("periods")
    @Expose
    private List<Period> periods = new ArrayList<Period>();

    public Integer getApplicableDays() {
        return applicableDays;
    }

    public void setApplicableDays(Integer applicableDays) {
        this.applicableDays = applicableDays;
    }

    public List<Period> getPeriods() {
        return periods;
    }

    public void setPeriods(List<Period> periods) {
        this.periods = periods;
    }

}
