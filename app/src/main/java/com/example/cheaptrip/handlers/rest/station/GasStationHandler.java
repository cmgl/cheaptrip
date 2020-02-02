package com.example.cheaptrip.handlers.rest.station;

import android.util.Log;

import com.example.cheaptrip.dao.rest.ODSStationClient;
import com.example.cheaptrip.handlers.rest.RestHandler;
import com.example.cheaptrip.models.TripGasStation;
import com.example.cheaptrip.models.tankerkoenig.ODSResponse;
import com.example.cheaptrip.models.tankerkoenig.Station;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class GasStationHandler extends RestHandler<List<Station>,String> {
    private final static String BASE_URL = "http://data.cheaptrip.cf/";
    private ODSStationClient gasStationClient;

    public GasStationHandler() {
        super(BASE_URL);

        gasStationClient = super.getRetrofit().create(ODSStationClient.class);
        Call call = gasStationClient.getStationData();
        super.setCall(call);
    }

    /**
     * TODO: Document
     * @param response Response from Rest-api of webservice.
     * @return
     */
    @Override
    public List<Station> extractDataFromResponse(Response<String> response) {
        String json = response.body();

        if(json == null ){
            Log.e("CHEAPTRIP","GasStationHandler->extractDataFromResponse: response is null");
            return null;
        }

        json = json.replace("\"\",","\"n/a\",");        // replace empty Entries
        json = json.replace("\"\"","\\\"");             // escape double quotes

        Gson gson = new Gson();

        ODSResponse odsResponse = gson.fromJson(json,ODSResponse.class);

        List<Station> stations = odsResponse.getData();


       /* List<TripGasStation> tripGasStationList = new ArrayList<>();

        for(Station station : stations) {
            TripGasStation tripGasStation = new TripGasStation(station);
            tripGasStationList.add(tripGasStation);
        }
*/

        return stations;
    }

}
