package com.example.cheaptrip.handlers.rest.geo;

import android.util.Log;


import com.example.cheaptrip.dao.rest.ORServiceClient;
import com.example.cheaptrip.handlers.rest.RestHandler;
import com.example.cheaptrip.models.TripGasStation;
import com.example.cheaptrip.models.TripLocation;
import com.example.cheaptrip.models.TripRoute;
import com.example.cheaptrip.models.orservice.GeoMatrixResponse;
import com.example.cheaptrip.models.orservice.MatrixPostBody;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class GeoDirectionMatrixHandler extends RestHandler<List<TripRoute>,GeoMatrixResponse>{

    private static String BASE_URL = "https://api.openrouteservice.org/v2/";
    private static ORServiceClient orServiceClient;

    List<TripLocation> mTripLocationList;
    private List<Integer> mSources;
    private boolean mSetStops;

    public GeoDirectionMatrixHandler(List<TripLocation> tripLocationList, List<Integer> sources, List<Integer> destinations, boolean bSetStops) {
        super(BASE_URL);
        mTripLocationList = tripLocationList;
        mSources = sources;
        mSetStops = bSetStops;

        orServiceClient = super.getRetrofit().create(ORServiceClient.class);

        String body = prepareBody(tripLocationList, sources, null);
        Call call = orServiceClient.getMatrix(body);
        super.setCall(call);

    }

    @Override
    public List<TripRoute> extractDataFromResponse(Response<GeoMatrixResponse> response) {
        GeoMatrixResponse geoMatrixResponse = response.body();

        if(geoMatrixResponse == null){
            Log.e("CHEAPTRIP","GeoMatrixHandler->extractDataFromResponse: GeoMatrix Response is null");
            return null;
        }

        List<TripRoute> tripRouteList = new ArrayList<>();

        List<List<Double>> distances = geoMatrixResponse.getDistances();
        List<List<Double>> durations = geoMatrixResponse.getDurations();

        if(distances == null || distances.isEmpty()){
            Log.e("CHEAPTRIP","GeoMatrixHandler->extractDataFromResponse: Distances List is null");
            return null;
        }

        if(durations == null || durations.isEmpty()){
            Log.e("CHEAPTRIP","GeoMatrixHandler->extractDataFromResponse: Durations List is null");
            return null;
        }

        if(distances.size() != durations.size()){
            Log.e("CHEAPTRIP","GeoMatrixHandler->extractDataFromResponse: Distances List and Duration List size differ");
            return null;
        }

        /*=======================================================================================
         * Generate Sum of duration and distance for Every Route ( Start -> Station -> End)
         *=======================================================================================*/
        List<Double> distanceSumList = new ArrayList<>(Collections.nCopies(distances.get(0).size(), 0.0));
        List<Double> durationSumList = new ArrayList<>(Collections.nCopies(durations.get(0).size(),0.0));


        for(int i = 0; i < distances.size();i++){

            List<Double> distancesForPoint = distances.get(i);
            List<Double> durationsForPoint = durations.get(i);

            for(int j = 0 ; j <distances.get(0).size(); j++ ){

                Double distance = distancesForPoint.get(j);
                Double duration = durationsForPoint.get(j);

                double distanceSum = distanceSumList.get(j) + distance;
                distanceSumList.set(j,distanceSum);

                double durationSum = durationSumList.get(j) + duration;
                durationSumList.set(j,durationSum);
            }
        }
        /*=================================================================================
         * Init TripRouteObject and pass values to it (Ignore first two entries
         * ( properties between other point and self)
         *=================================================================================*/
        for(int i = distances.size() ;i < distanceSumList.size() ; i++){
            double distance = distanceSumList.get(i);
            double duration = durationSumList.get(i);

            TripRoute tripRoute = new TripRoute();
            tripRoute.setDistance(distance);
            tripRoute.setDuration(duration);
            /*===================================================
             * Set the tripLocations
             * (consisting of the sources one of the other locations)
             *===================================================*/

            if(mSetStops) {
                List<TripLocation> stops = new ArrayList<>();
                // Add the sources (Start + End Location)
                for (int index : mSources) {
                    stops.add(mTripLocationList.get(index));
                }

                // Add the intermediate location (=GasStation)
                if (mTripLocationList.get(i) instanceof TripGasStation) {
                    TripGasStation tripGasStation = (TripGasStation) mTripLocationList.get(i);
                    stops.add(tripGasStation);
                }

                tripRoute.setStops(stops);
            }


            // Add to the List
            tripRouteList.add(tripRoute);
        }

        return tripRouteList;
    }

    private String prepareBody(List<TripLocation> tripLocationList,List<Integer> sources, List<Integer> destinations) {
        List<List<Double>> coordinates = TripLocation.getAsDoubleList(tripLocationList);

        MatrixPostBody postBody = new MatrixPostBody(
                coordinates,
                sources,
                MatrixPostBody.UNITS.km
        );

        Gson gson = new Gson();
        String body = gson.toJson(postBody);



        return body;
    }
}