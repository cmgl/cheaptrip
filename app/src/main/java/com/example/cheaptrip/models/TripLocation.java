package com.example.cheaptrip.models;

import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.cheaptrip.models.photon.Location;
import com.example.cheaptrip.models.photon.Properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "TRIP_LOCATION")
public class TripLocation implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name= "LOCATION_NAME")
    private String locationName;

    @ColumnInfo(name= "LATITUDE")
    private double latitdue;

    @ColumnInfo(name= "LONGITUDE")
    private double longitude;

    @ColumnInfo(name="CITY")
    private String city;

    @ColumnInfo(name="COUNTRY")
    private String country;

    @ColumnInfo(name="NAME")
    private String name;

    @ColumnInfo(name="POSTCODE")
    private String postcode;

    @ColumnInfo(name="STREET")
    private String street;

    @ColumnInfo(name="HOUSENUMBER")
    private String housenumber;


    public TripLocation(String locationName, double latitdue, double longitude) {
        this.locationName = locationName;
        this.latitdue = latitdue;
        this.longitude = longitude;
    }

    public TripLocation(Location location){
        if(location == null){
            String msg = "Cannot initialize TripLocation when location is null";
            Log.e("TripLocation",msg);
            throw new IllegalArgumentException(msg);
        }

        /*====================================================
         * Set Longitude and Latitude
         *====================================================*/
        List<Double> coordinates = location.getCoordinates();

        if(coordinates == null || coordinates.isEmpty()){
            String msg = "Provided Argument location cannot have no coordinates.";
            Log.e("TripLocation",msg);
            throw new IllegalArgumentException(msg);
        }

        double lon = coordinates.get(0);    // longitude of the location
        double lat = coordinates.get(1);    // latitude of the location

        setLongitude(lon);
        setLatitdue(lat);
        /*====================================================
         * Set Location Names (Street postal ...)
         *====================================================*/
        Properties properties = location.getProperties();

        if(properties == null){
            String msg = "Provided Argument location cannot have no Properties.";
            Log.e("TripLocation",msg);
            throw new IllegalArgumentException(msg);
        }

        setCountry(properties.getCountry());
        setCity(properties.getCity());
        setPostcode(properties.getPostcode());
        setStreet(properties.getStreet());
        setHousenumber(properties.getHousenumber());
        setLocationName(properties.getName());



    }

    public TripLocation(List<Double> coordinate){
        assert(coordinate.size() == 2);
        assert(coordinate.get(0) != null);
        assert(coordinate.get(1) != null);

        double longitude = coordinate.get(0);
        double latitude = coordinate.get(1);


        this.latitdue = latitude;
        this.longitude = longitude;

        assertInvariants();

    }

    public TripLocation(double latitdue, double longitude){
        assertInvariants();
        this.latitdue = latitdue;
        this.longitude = longitude;
        assertInvariants();
    }

    public TripLocation(){
        assertInvariants();
    }

    public double getLongitude() {
        assertInvariants();
        return longitude;
    }

    public void setLongitude(double longitude) {
        assertInvariants();
        this.longitude = longitude;
        assertInvariants();
    }

    public double getLatitdue() {
        assertInvariants();
        return latitdue;
    }

    public void setLatitdue(double latitdue) {
        assertInvariants();
        this.latitdue = latitdue;
        assertInvariants();
    }

    public String getLocationName() {
        assertInvariants();
        return locationName;
    }

    public void setLocationName(String locationName) {
        assertInvariants();
        this.locationName = locationName;
        assertInvariants();
    }

    public String getCity() {
        assertInvariants();
        return city;
    }

    public void setCity(String city) {
        assertInvariants();
        this.city = city;
        assertInvariants();
    }

    public String getCountry() {
        assertInvariants();
        return country;
    }

    public void setCountry(String country) {
        assertInvariants();
        this.country = country;
        assertInvariants();
    }

    public String getName() {
        assertInvariants();
        return name;
    }

    public void setName(String name) {
        assertInvariants();
        this.name = name;
        assertInvariants();
    }

    public String getPostcode() {
        assertInvariants();
        return postcode;
    }

    public void setPostcode(String postcode) {
        assertInvariants();
        this.postcode = postcode;
        assertInvariants();
    }

    public String getStreet() {
        assertInvariants();
        return street;
    }

    public void setStreet(String street) {
        assertInvariants();
        this.street = street;
        assertInvariants();
    }

    public String getHousenumber() {
        assertInvariants();
        return housenumber;
    }

    public void setHousenumber(String housenumber) {
        assertInvariants();
        this.housenumber = housenumber;
        assertInvariants();
    }

    public List<Double> getAsDoubleList(){
        assertInvariants();

        List<Double> coordinates = new ArrayList<>();

        coordinates.add(longitude);
        coordinates.add(latitdue);

        assertInvariants();
        return coordinates;
    }

    public static List<TripLocation> getAsTripLocationList(List<List<Double>> coordinates){
        List<TripLocation> tripLocationList = new ArrayList<>();

        if (coordinates == null){
            Log.e("CHEAPTRIP","getAsTripLocationList(): cannot convert List: List is null");
            return null;
        }

        if (coordinates.size() == 0){
            Log.w("CHEAPTRIP", "getAsTripLocationList(): Converting empty List");
        }

        for(List<Double> coordinate : coordinates){
            TripLocation tripLocation = new TripLocation(coordinate);
            tripLocationList.add(tripLocation);
        }

        return tripLocationList;
    }

    public static List<List<Double>> getAsDoubleList(List<TripLocation> tripLocations){
        List<List<Double>> coordinateList = new ArrayList<>();

        if (tripLocations == null){
            Log.e("CHEAPTRIP","getAsDoubleList(): cannot convert List: List is null");
            return null;
        }

        if (tripLocations.size() == 0){
            Log.w("CHEAPTRIP", "getAsDoubleList(): Converting empty List");
        }

        for(TripLocation tripLocation : tripLocations){

            List<Double> doubleList = new ArrayList<>();
            doubleList.add(tripLocation.getLongitude());
            doubleList.add(tripLocation.getLatitdue());

            coordinateList.add(doubleList);

        }

        return coordinateList;
    }


    public static List<List<Double>> getAsDoubleList(TripLocation ... tripLocations ){
        List<List<Double>> coordinateList = new ArrayList<>();

        if (tripLocations == null){
            Log.e("CHEAPTRIP","getAsDoubleList(): cannot convert List: List is null");
            return null;
        }


        for(TripLocation tripLocation : tripLocations){

            List<Double> doubleList = new ArrayList<>();
            doubleList.add(tripLocation.getLongitude());
            doubleList.add(tripLocation.getLatitdue());

            coordinateList.add(doubleList);

        }

        return coordinateList;
    }


    public String getInfoWindowText(){
        String labelText = "";
        if (postcode != null && postcode.length() > 0){
            labelText += "";
        }


        if (city != null && city.length() > 0){
            labelText += postcode + " " + city;
        }


        if (locationName != null && locationName.length() > 0){
            labelText += "\n" + locationName;
        }


        if (street != null && street.length() > 0){
            labelText += "\n" + street;
        }


        if (housenumber != null){
            labelText += " " + housenumber;
        }


        if (labelText.trim().length() == 0)
            return null;

        return labelText;
    }



    private void assertInvariants(){
        assert (longitude >= 0);
        assert (longitude >= 0);
    }


}
