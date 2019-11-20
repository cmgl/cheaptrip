package com.example.cheaptrip.handlers.rest.geo;

import com.example.cheaptrip.dao.GeoCompletionClient;
import com.example.cheaptrip.handlers.rest.RestHandler;
import com.example.cheaptrip.models.rest.photon.Feature;
import com.example.cheaptrip.models.rest.photon.PhotonResponse;
import com.example.cheaptrip.models.rest.photon.Properties;


import retrofit2.Response;

public class GeoNameRestHandler extends RestHandler<String,PhotonResponse> {
    private final static String BASE_URL = "http://photon.komoot.de/";

    public GeoNameRestHandler(double lat, double lon){
        super(BASE_URL);

        GeoCompletionClient geoCompletionClient = retrofit.create(GeoCompletionClient.class);
        call = geoCompletionClient.getLocationName(lat,lon);
    }


    /**
     * TODO:Document
     * @param response Response from Rest-api of webservice.
     * @return
     */
    @Override
    public String extractDataFromResponse(Response<PhotonResponse> response) {
        PhotonResponse photonResponse = response.body();
        Feature feature = photonResponse.getFeatures().get(0);

        String locationName;

        if (feature == null) {
            return null;
        }

        Properties properties = feature.getProperties();
        String city = properties.getCity();
        if (city == null){
            city = "";
        }

        String name = properties.getName();
        if (name == null){
            name = "";
        }

//        String country = properties.getCountry();
//        locationName  = city + "," + name + "(" + country + ")";

        locationName = city + "," + name;

        return locationName;
    }
}