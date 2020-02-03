package com.example.cheaptrip.handlers.rest.geo;

import android.util.Log;

import com.example.cheaptrip.dao.rest.GeoCompletionClient;
import com.example.cheaptrip.handlers.rest.RestHandler;
import com.example.cheaptrip.models.TripLocation;
import com.example.cheaptrip.models.photon.Location;
import com.example.cheaptrip.models.photon.PhotonResponse;


import retrofit2.Call;
import retrofit2.Response;


/**
 * This Rest-Handler gets and extracts Location-Names for
 */
public class GeoNameForLocationHandler extends RestHandler<TripLocation,PhotonResponse> {
    private final static String BASE_URL = "http://photon.komoot.de/";
    private static GeoCompletionClient geoCompletionClient;


    public GeoNameForLocationHandler(double lat, double lon){
        super(BASE_URL);

        geoCompletionClient = super.getRetrofit().create(GeoCompletionClient.class);
        Call call = geoCompletionClient.getLocationName(lat,lon);
        super.setCall(call);
    }

    /**
     * TODO:Document
     * @param response Response from Rest-api of webservice.
     * @return
     */
    @Override
    public TripLocation extractDataFromResponse(Response<PhotonResponse> response) {
        PhotonResponse photonResponse = response.body();

        if (photonResponse == null) {
            Log.e("CHEAPTRIP", "Cannot extract Name for Location: REST-Response is null");
            return null;
        }

        if (photonResponse.getLocations() == null) {
            Log.e("CHEAPTRIP", "Cannot extract Name for Location: REST-Response has no Locations.");
            return null;
        }

        if (photonResponse.getLocations().size() < 1) {
            Log.e("CHEAPTRIP", "Cannot extract Name for Location: REST-Response has empty LocationList.");
            return null;
        }
        Location location = photonResponse.getLocations().get(0);

        return new TripLocation(location);

    }
}
