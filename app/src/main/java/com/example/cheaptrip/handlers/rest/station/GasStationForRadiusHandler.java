package com.example.cheaptrip.handlers.rest.station;

import android.content.Context;
import android.util.Log;

import com.example.cheaptrip.dao.database.GasStationDatabaseClient;
import com.example.cheaptrip.dao.rest.GasStationClient;
import com.example.cheaptrip.database.GasStationDatabase;
import com.example.cheaptrip.handlers.rest.RestHandler;
import com.example.cheaptrip.models.TripGasStation;
import com.example.cheaptrip.models.tankerkoenig.GasStationResponse;
import com.example.cheaptrip.models.tankerkoenig.Station;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class GasStationForRadiusHandler extends RestHandler<List<TripGasStation>,GasStationResponse> {
    // TODO: https://dev.azure.com/tankerkoenig/362e70d1-bafa-4cf7-a346-1f3613304973/_apis/git/repositories/0d6e7286-91e4-402c-af56-fa75be1f223d/items?path=%2Fprices%2F2019%2F12&versionType=Branch
    private final static String BASE_URL = "https://creativecommons.tankerkoenig.de/";
    private final static String API_KEY = "ad5b5cac-db85-4516-832d-1bc90df23946";
    private Context mContext;


    public GasStationForRadiusHandler(Context context, double lat, double lon, double radius, GasStationClient.FuelType fuelType) {
        super(BASE_URL);
        mContext = context;
        GasStationClient gasStationClient = super.getRetrofit().create(GasStationClient.class);
        Call call = gasStationClient.getStationsInRadius(lat,lon,radius, "all", "dist");

        super.setCall(call);
    }

    /**
     * TODO: Document
     * @param response Response from Rest-api of webservice.
     * @return
     */
    @Override
    public List<TripGasStation> extractDataFromResponse(Response<GasStationResponse> response) {
        GasStationResponse gasStationResponse = response.body();

        List<TripGasStation> tripGasStationList = new ArrayList<>();


        List<Station> stations = gasStationResponse.getStations();

        exchangeWithDatabase(stations);

        for(Station station : stations){
            tripGasStationList.add(new TripGasStation(station));
        }

        return tripGasStationList;
    }

    /**
     * Populates the stations in the list with missing information from database
     * and populates the database with the Current Prices.
     *
     * @param stationList      Stations that exchange information with the database
     */
    private void exchangeWithDatabase(List<Station> stationList) {

        if (mContext == null) {
            Log.e("CHEAPTRIP", "GasStationForRadiusHandler-> exchangeWithDatabase(): Context is null");
            return;
        }
        GasStationDatabase gasStationDatabase = GasStationDatabase.getDatabase(mContext);

        if (gasStationDatabase == null) {
            Log.e("CHEAPTRIP", "GasStationForRadiusHandler-> exchangeWithDatabase(): Cannot init gasStationDatabase");
            return;
        }

        GasStationDatabaseClient gasStationDatabaseClient = gasStationDatabase.gasStationDatabaseClient();

        if (gasStationDatabaseClient == null) {
            Log.e("CHEAPTRIP", "GasStationForRadiusHandler-> exchangeWithDatabase(): Cannot init GasStationDatabaseClient");
            return;
        }

        //gasStationDatabaseClient.updateAll(stationList);

        for(Station station: stationList) {
            Station stationFromDB = gasStationDatabaseClient.getForID(station.getId());
            station.setOpeningtimes_json(stationFromDB.getOpeningtimes_json());
        }

        gasStationDatabase.close();
    }
}
