package com.example.cheaptrip.models.tankerkoenig;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


@Entity(tableName = "STATIONS")
public class Station {
    @NonNull
    @PrimaryKey()
    @SerializedName(value="id", alternate={"station_uuid","uuid"})
    @Expose
	@ColumnInfo(name="ID")
    private String id;

    @SerializedName("name")
    @Expose
	@ColumnInfo(name="name")
    private String name;

    @SerializedName("brand")
    @Expose
	@ColumnInfo(name="brand")
    private String brand;

    @SerializedName("street")
    @Expose
	@ColumnInfo(name="street")
    private String street;

    @SerializedName("place")
    @Expose
	@ColumnInfo(name="place")
    private String place;

    @SerializedName("lat")
    @Expose
	@ColumnInfo(name="lat")
    private double lat;

    @SerializedName("lng")
    @Expose
	@ColumnInfo(name="lng")
    private double lng;

    @SerializedName("dist")
    @Expose
	@ColumnInfo(name="dist")
    private double dist;

    @SerializedName("diesel")
    @Expose
	@ColumnInfo(name="diesel")
    private double diesel;

    @SerializedName("e5")
    @Expose
	@ColumnInfo(name="e5")
    private double e5;

    @SerializedName("e10")
    @Expose
	@ColumnInfo(name="e10")
    private double e10;

    @SerializedName("isOpen")
    @Expose
	@ColumnInfo(name="isOpen")
    private Boolean isOpen;

    @SerializedName("houseNumber")
    @Expose
	@ColumnInfo(name="houseNumber")
    private String houseNumber;

    @SerializedName("postCode")
    @Expose
	@ColumnInfo(name="postCode")
    private Integer postCode;

    @SerializedName("date")
    @ColumnInfo(name="DATE")
    private String date;

    @SerializedName("first_active")
    @Expose
	@ColumnInfo(name="first_active")
    private String firstActive;

    @SerializedName("openingtimes_json")
    @ColumnInfo(name="OPENING_TIMES")
    private String openingtimes_json;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getDist() {
        return dist;
    }

    public void setDist(double dist) {
        this.dist = dist;
    }

    public double getDiesel() {
        return diesel;
    }

    public void setDiesel(double diesel) {
        this.diesel = diesel;
    }

    public double getE5() {
        return e5;
    }

    public void setE5(double e5) {
        this.e5 = e5;
    }

    public double getE10() {
        return e10;
    }

    public void setE10(double e10) {
        this.e10 = e10;
    }

    public Boolean isOpen() {
        return isOpen;
    }

    public void setIsOpen(Boolean isOpen) {
        this.isOpen = isOpen;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public Integer getPostCode() {
        return postCode;
    }

    public void setPostCode(Integer postCode) {
        this.postCode = postCode;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOpeningtimes_json() {
        return openingtimes_json;
    }

    public void setOpeningtimes_json(String openingtimes_json) {
        this.openingtimes_json = openingtimes_json;
    }

    public String getFirstActive() {
        return firstActive;
    }

    public void setFirstActive(String firstActive) {
        this.firstActive = firstActive;
    }
}