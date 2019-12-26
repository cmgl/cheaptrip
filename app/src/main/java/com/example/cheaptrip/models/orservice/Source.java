package com.example.cheaptrip.models.orservice;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Source {

    @SerializedName("location")
    @Expose
    private List<Double> location = new ArrayList<Double>();
    @SerializedName("snapped_distance")
    @Expose
    private Double snappedDistance;

    public List<Double> getLocation() {
        return location;
    }

    public void setLocation(List<Double> location) {
        this.location = location;
    }

    public Double getSnappedDistance() {
        return snappedDistance;
    }

    public void setSnappedDistance(Double snappedDistance) {
        this.snappedDistance = snappedDistance;
    }

}