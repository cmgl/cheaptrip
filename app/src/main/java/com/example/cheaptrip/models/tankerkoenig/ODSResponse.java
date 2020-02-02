package com.example.cheaptrip.models.tankerkoenig;


import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ODSResponse {

    @SerializedName("id")
    @Expose
    private Integer id;

    @SerializedName("data")
    @Expose
    private List<Station> data = new ArrayList<Station>();

    @SerializedName("timestamp")
    @Expose
    private String timestamp;

    @SerializedName("origin")
    @Expose
    private String origin;

    @SerializedName("license")
    @Expose
    private String license;

    @SerializedName("pipelineId")
    @Expose
    private String pipelineId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<Station> getData() {
        return data;
    }

    public void setData(List<Station> data) {
        this.data = data;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(String pipelineId) {
        this.pipelineId = pipelineId;
    }

}