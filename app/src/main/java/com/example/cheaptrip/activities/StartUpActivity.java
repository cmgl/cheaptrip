package com.example.cheaptrip.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.example.cheaptrip.R;
import com.example.cheaptrip.app.CheapTripApp;
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
import com.example.cheaptrip.services.DatabasePopulationService;

import java.util.List;

/**
 * This class represents the splash screen when starting the app.
 * It takes care of updating the Vehicle Database
 * by getting the current VehicleDataset from JValue ODS.
 *
 */
public class StartUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CheapTripApp)getApplication()).setCurrentActivity(this);

        setContentView(R.layout.activity_startup);

        DatabasePopulationService databasePopulationService = new DatabasePopulationService(this, new StartupListener() {
            @Override
            public void onVehiclesLoaded() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        databasePopulationService.execute();

    }

    /**
     * Called on Destruction of the Activity
     * The Activity gets removed from the stack -> registers removal to the app
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        CheapTripApp cheapTripApp = (CheapTripApp) getApplication();
        Activity currActivity = cheapTripApp.getCurrentActivity() ;

        if ( this .equals(currActivity))
            cheapTripApp.setCurrentActivity( null ) ;
    }
    /**
     * Called on Resume of the Activity
     * The Activity will be added on top of the stack (-> registration) to the app
     */
    public void onResume(){
        super.onResume();

        CheapTripApp cheapTripApp = (CheapTripApp) getApplication();
        cheapTripApp .setCurrentActivity( this ) ;
    }

    /**
     * Called on Pause of the Activity
     * The Activity will be removed from top of the stack (-> registration to the app)
     */
    public void onPause(){
        super.onPause();

        CheapTripApp cheapTripApp = (CheapTripApp) getApplication();
        Activity currActivity = cheapTripApp.getCurrentActivity() ;

        if ( this .equals(currActivity))
            cheapTripApp.setCurrentActivity( null ) ;
    }

}
