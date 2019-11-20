package com.example.cheaptrip.handlers.rest.geo;

import com.example.cheaptrip.dao.GeoCompletionClient;
import com.example.cheaptrip.handlers.rest.RestHandler;
import com.example.cheaptrip.models.rest.photon.Feature;
import com.example.cheaptrip.models.rest.photon.Geometry;
import com.example.cheaptrip.models.rest.photon.PhotonResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class GeoLocationRestHandler extends RestHandler<List<List<Double>> ,PhotonResponse> {
    private final static String BASE_URL = "http://photon.komoot.de/";


    public GeoLocationRestHandler(String locationName){
        super(BASE_URL);
        GeoCompletionClient geoCompletionClient = retrofit.create(GeoCompletionClient.class);
        super.call = geoCompletionClient.geoPos(locationName);
    }
    /**
     * TODO: Document
     *
     */
    public GeoLocationRestHandler(String locationName ,double lat , double lon) {
        super(BASE_URL);
        GeoCompletionClient geoCompletionClient = retrofit.create(GeoCompletionClient.class);
        super.call = geoCompletionClient.geoPos(locationName,lat,lon);
    }

    /**
     * TODO: Document
     * @param response
     * @return
     */
    @Override
    public List<List<Double>> extractDataFromResponse(Response<PhotonResponse> response) {
        PhotonResponse photonResponse = response.body();
        List<Feature> features = photonResponse.getFeatures();

        List<List<Double>> coordinates = new ArrayList<>();

        for (Feature feature : features){
            Geometry geometry = feature.getGeometry();
            List<Double> currCoordinates = geometry.getCoordinates();

            coordinates.add(currCoordinates);
        }

        return coordinates;
    }
}