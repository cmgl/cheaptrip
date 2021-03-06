package com.example.cheaptrip.models.photon;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Location {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("geometry")
    @Expose
    private Geometry geometry;
    @SerializedName("properties")
    @Expose
    private Properties properties;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public List<Double> getCoordinates(){
        return getGeometry().getCoordinates();
    }

    public String getCountry(){
        return getProperties().getCountry();
    }

    public String getCity(){
        return getProperties().getCity();
    }

    public String getPostcode(){
        return getProperties().getPostcode();
    }

    public String getName(){
        return getProperties().getName();
    }

    public String getStringForList(){
        String textField = "";

        Properties properties = getProperties();
        /*------------------------------------------
         *Add City to result text
         *------------------------------------------*/
        String city = properties.getCity();
        if (city != null){
            textField += city + ", ";
        }
        /*------------------------------------------
         * Add Name to result text
         *------------------------------------------*/
        String name = properties.getName();
        if (name != null){
            textField += name + ", ";
        }
        /*------------------------------------------
         * Add Street and HouseNumber to result text
         *-----------------------------------------*/
        String street = properties.getStreet();
        if (street != null){
            textField += street;

            String houseNumber = properties.getHousenumber();

            if(houseNumber != null){
                textField +=  " " + houseNumber;
            }
        }

        return textField;
    }


}