package com.example.cheaptrip.handlers.rest.geo;

import android.util.Log;

import com.example.cheaptrip.dao.rest.ORServiceClient;
import com.example.cheaptrip.handlers.rest.RestHandler;
import com.example.cheaptrip.models.TripLocation;
import com.example.cheaptrip.models.TripRoute;
import com.example.cheaptrip.models.orservice.Feature;
import com.example.cheaptrip.models.orservice.Geometry;
import com.example.cheaptrip.models.orservice.ORServiceResponse;
import com.example.cheaptrip.models.orservice.PostBody;
import com.example.cheaptrip.models.orservice.Properties;
import com.example.cheaptrip.models.orservice.Segment;
import com.example.cheaptrip.models.orservice.Summary;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class GeoDirectionsHandler extends RestHandler<TripRoute,String> {
    private static final String BASE_URL = "https://api.openrouteservice.org/v2/";

    private List<TripLocation> tripLocationList;
    private TripRoute mTripRoute;

    public GeoDirectionsHandler(List<TripLocation> tripLocationList, TripRoute tripRoute) {
        super(BASE_URL);
        this.tripLocationList = tripLocationList;
        mTripRoute = tripRoute;

        ORServiceClient orServiceClient = super.getRetrofit().create(ORServiceClient.class);

        String requestBody = generateBody(tripLocationList);
        Call<String> call = orServiceClient.getGeoJson(requestBody);
        super.setCall(call);
    }


    @Override
    public TripRoute extractDataFromResponse(Response<String> response) {
        String geoJSON = response.body();


        Gson gson = new Gson();
        TripRoute tripRoute;

        // Create a copy
        if(mTripRoute != null){
            tripRoute = new TripRoute(mTripRoute);
        }else{
            tripRoute = new TripRoute();
        }

        if(geoJSON == null){
            Log.e("CHEAPTRIP","GeoDirectionsHandler#extractDataFromResponse(): Cannot extract geoJSON from ORService.");
            return null;
        }

        ORServiceResponse orServiceResponse = gson.fromJson(geoJSON,ORServiceResponse.class);


        if(orServiceResponse == null){
            Log.e("CHEAPTRIP","GeoDirectionsHandler#extractDataFromResponse(): Cannot convert JSON to Object using GSON");
            return null;
        }

        List<Feature> features = orServiceResponse.getFeatures();

        if(features == null || features.isEmpty()){
            Log.e("CHEAPTRIP","GeoDirectionsHandler#extractDataFromResponse(): Cannot extract features from orServiceResponse");
            return null;
        }

        Geometry geometry = features.get(0).getGeometry();

        if(geometry == null){
            Log.e("CHEAPTRIP","GeoDirectionsHandler#extractDataFromResponse(): Cannot extract geometry from features");
            return null;
        }


        List<List<Double>> pointsForPolyLine = geometry.getCoordinates();

        if(pointsForPolyLine == null || pointsForPolyLine.isEmpty()){
            Log.e("CHEAPTRIP","GeoDirectionsHandler#extractDataFromResponse(): Cannot extract coordinates from geometry");
            return null;
        }

        Properties properties = features.get(0).getProperties();

        if(properties == null){
            Log.e("CHEAPTRIP","GeoDirectionsHandler#extractDataFromResponse(): Cannot extract properties from features");
            return null;
        }

        Summary summary = properties.getSummary();

        if(summary == null){
            Log.e("CHEAPTRIP","GeoDirectionsHandler#extractDataFromResponse(): Cannot extract summary from properties");
            return null;
        }

        List<Segment> segments = properties.getSegments();

        if(segments == null || segments.isEmpty()){
            Log.e("CHEAPTRIP","GeoDirectionsHandler#extractDataFromResponse(): Cannot extract segments from properties");
            return null;
        }

        Double distance = summary.getDistance();

        if(distance == null){
            Log.e("CHEAPTRIP","GeoDirectionsHandler#extractDataFromResponse(): Cannot extract distance from summary");
            return null;
        }

        Double duration = summary.getDuration();

        if(duration == null){
            Log.e("CHEAPTRIP","GeoDirectionsHandler#extractDataFromResponse(): Cannot extract duration from summary");
            return null;
        }


        tripRoute.setRouteSegments((ArrayList<Segment>) segments);
        tripRoute.setGeoJSON(geoJSON);
        tripRoute.setDistance(distance);
        tripRoute.setDuration(duration);

        tripRoute.setPointsForPolyLine(TripLocation.getAsTripLocationList(pointsForPolyLine));

        return tripRoute;
    }

    private String generateBody(List<TripLocation> tripLocationList){

        if(tripLocationList == null){
            Log.e("CHEAPTRIP", "Cannot create Body: mTripLocationList is null");
            return null;
        }

        List<List<Double>> coordinates = TripLocation.getAsDoubleList(tripLocationList);

        PostBody postBody = new PostBody(coordinates,"km");
        Gson gson = new Gson();
        String json = gson.toJson(postBody);

        return json;
    }

}
