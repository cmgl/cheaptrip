package com.example.cheaptrip.models;

import com.example.cheaptrip.models.tankerkoenig.OpeningTime;
import com.example.cheaptrip.models.tankerkoenig.OpeningTimes;
import com.example.cheaptrip.models.tankerkoenig.Station;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TripGasStation extends TripLocation {

    private Double mPriceE5;
    private Double mPriceE10;
    private Double mPriceDiesel;

    private String mBrand;

    private Boolean bIsOpen;


    private OpeningTimes openingTimes;

    public OpeningTimes getOpeningTimes() {
        return openingTimes;
    }


    public TripGasStation(Station station) {

        double lat = station.getLat();
        double lon  = station.getLng();
        String name = station.getName();


        String place = station.getPlace();
        String houseNumber = station.getHouseNumber();
        String street = station.getStreet();
        Integer postcode = station.getPostCode();

        setLatitdue(lat);
        setLongitude(lon);
        setName(name);

        setLocationName(name);
        setStreet(street);
        setHousenumber(houseNumber);
        setPostcode(""+postcode);
        setCity(place);

        mPriceE5 = station.getE5();
        mPriceE10 = station.getE10();
        mPriceDiesel = station.getDiesel();
        mBrand = station.getBrand();
        bIsOpen = station.isOpen();

        Gson gson = new Gson();
        String strOpeningTime = station.getOpeningtimes_json();
        //strOpeningTime = strOpeningTime.substring(1,strOpeningTime.length()-2); // remove cursive brackets --> make it list

        openingTimes =  gson.fromJson(strOpeningTime,OpeningTimes.class);
    }

    public double getPriceE5() {
        return mPriceE5;
    }

    public void setPriceE5(double mPriceE5) {
        this.mPriceE5 = mPriceE5;
    }

    public double getPriceE10() {
        return mPriceE10;
    }

    public void setPriceE10(double mPriceE10) {
        this.mPriceE10 = mPriceE10;
    }

    public double getPriceDiesel() {
        return mPriceDiesel;
    }

    public void setPriceDiesel(double mPriceDiesel) {
        this.mPriceDiesel = mPriceDiesel;
    }

    public String getBrand() {
        return mBrand;
    }

    public void setBrand(String mBrand) {
        this.mBrand = mBrand;
    }

    public boolean isOpen() {
        return bIsOpen;
    }

    public void setOpeningTimes(OpeningTimes openingTimes) {
        this.openingTimes = openingTimes;
    }

}
