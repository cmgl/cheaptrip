package com.example.cheaptrip.services;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.cheaptrip.dao.database.GasStationDatabaseClient;
import com.example.cheaptrip.database.GasStationDatabase;
import com.example.cheaptrip.database.VehicleDatabase;
import com.example.cheaptrip.handlers.StartupListener;
import com.example.cheaptrip.handlers.rest.RestListener;
import com.example.cheaptrip.handlers.rest.station.GasStationHandler;
import com.example.cheaptrip.handlers.rest.station.GasStationHistoryHandler;
import com.example.cheaptrip.handlers.rest.station.GasStationHistoryPriceHandler;
import com.example.cheaptrip.handlers.rest.vehicle.VehicleDataSetHandler;
import com.example.cheaptrip.models.TripGasStation;
import com.example.cheaptrip.models.fueleconomy.VehicleDataSet;
import com.example.cheaptrip.models.tankerkoenig.Station;

import java.util.Calendar;
import java.util.List;

public class DatabasePopulationService extends AsyncTask<Void,Void,Void>  {


    private Context mContext;
    private StartupListener mStartupListener;

    public DatabasePopulationService(Context context, StartupListener startupListener) {
        mContext = context;
        mStartupListener = startupListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        VehicleDataSetHandler vehicleDataSetHandler  = new VehicleDataSetHandler();
        VehicleDatabase vehicleDatabase = VehicleDatabase.getDatabase(mContext);

        List<VehicleDataSet> vehicleDataSetList = vehicleDataSetHandler.makeSyncRequest();
        List<VehicleDataSet> fromDataBase = vehicleDatabase.vehicleDatabaseClient().getAll();



        if(!fromDataBase.equals(vehicleDataSetList) && vehicleDataSetList != null && !vehicleDataSetList.isEmpty()){
            vehicleDatabase.vehicleDatabaseClient().deleteAll();
            vehicleDatabase.vehicleDatabaseClient().insertAll(vehicleDataSetList);
        }

        vehicleDatabase.close();


      /*  GasStationHandler gasStationHandler = new GasStationHandler();

        List<TripGasStation> tripGasStations = gasStationHandler.makeSyncRequest();

        tripGasStations.get(0);*/

        GasStationHandler stationHandler = new GasStationHandler();
        List<Station> stationList = stationHandler.makeSyncRequest();

        GasStationDatabase gasStationDatabase = GasStationDatabase.getDatabase(mContext);

        if(gasStationDatabase == null){
            Log.e("CHEAPTRIP","DatabasePopulationService: Cannot init gasStationDatabase");
            return null;
        }

        GasStationDatabaseClient gasStationDatabaseClient = gasStationDatabase.gasStationDatabaseClient();

        if(gasStationDatabaseClient == null){
            Log.e("CHEAPTRIP","DatabasePopulationService: Cannot init GasStationDatabaseClient");
            return null;
        }

        gasStationDatabaseClient.deleteAll();
        gasStationDatabaseClient.insertAll(stationList);

        gasStationDatabase.close();

/*
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month =  Calendar.getInstance().get(Calendar.MONTH);
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        GasStationHistoryHandler gasStationHistoryHandler = new GasStationHistoryHandler(year,month,day);

       List<Station> stationList = gasStationHistoryHandler.makeSyncRequest();

       GasStationDatabaseClient dbClient = GasStationDatabase.getDatabase(mContext).gasStationDatabaseClient();
       dbClient.insertAll(stationList);
*/




        /*gasStationHistoryPriceHandler.makeAsyncRequest(new RestListener<List<Station>>() {
            @Override
            public void OnRestSuccess(List<Station> stations) {

                    GasStationDatabaseClient dbClient = GasStationDatabase.getDatabase(mContext).gasStationDatabaseClient();
                    //dbClient.insertAll(stations);

            }

            @Override
            public void OnRestFail() {

            }
        });*/

      /*  List<Station> stationList = gasStationHistoryPriceHandler.makeSyncRequest();
        GasStationDatabaseClient dbClient = GasStationDatabase.getDatabase(mContext).gasStationDatabaseClient();
        //dbClient.insertAll(stations);*/


        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mStartupListener.onVehiclesLoaded();
    }

}
