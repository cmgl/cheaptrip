package com.example.cheaptrip.activities;


import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;

import android.content.pm.PackageManager;

import android.os.Bundle;


import android.util.Pair;
import android.view.MenuItem;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.EditText;

import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cheaptrip.BuildConfig;
import com.example.cheaptrip.R;


import com.example.cheaptrip.app.CheapTripApp;
import com.example.cheaptrip.dao.rest.GasStationClient;
import com.example.cheaptrip.dao.database.VehicleDatabaseClient;
import com.example.cheaptrip.database.VehicleDatabase;
import com.example.cheaptrip.models.TripLocation;
import com.example.cheaptrip.models.TripVehicle;
import com.example.cheaptrip.views.Gauge;
import com.example.cheaptrip.views.Navigation;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the main activity of this application.
 * Its purpose is to assign the TripObject,
 * such as
 *      * {@link TripVehicle}:          the Vehicle to be used for the route calculation
 *      * {@link TripLocation} start:   the Starting location of the route
 *      * {@link TripLocation} end:     the destination of the route
 *      * {@link Gauge} Tank Contents:  Remaining fuel capacity of the fuel tank.
 *
 * These get set by user input by either collecting it from the view on this Activity ( tankGauge)
 * or from other Activities:
 *      * {@link VehicleBrandActivity}  ( Activity to select the vehicle make)
 *      * {@link VehicleModelActivity}  ( Activity to select the vehicle model)
 *      * {@link VehicleYearActivity}   ( Activity to select the vehicle construction year)
 *      * {@link MapActivity} for start ( Activity to select the starting point of the route)
 *      * {@link MapActivity} for start ( Activity to select the end point/Destination of the route)
 *
 * After the previous mention selections were made the user may click the button find to start the
 * calculation.
 */
public class MainActivity extends AppCompatActivity {
    // Request Codes for the Acitivities
    final static int ACTIVITY_REQ_CODE_BRAND    = 1;
    final static int ACTIVITY_REQ_CODE_MODEL    = 2;
    final static int ACTIVITY_REQ_CODE_YEAR     = 3;
    final static int ACTIVITY_REQ_CODE_START    = 4;
    final static int ACTIVITY_REQ_CODE_END      = 5;
    final static int ACTIVITY_REQ_CODE_CALC     = 6;

    Gauge gauge;                // Tank capacity view

    Button btn_carBrand;        // Selection for car brands
    Button btn_carModel;        // Selection for car models
    Button btn_carYear;         // Selection for car construction year
    Spinner spin_carFuel;       // Spinner (Drop Down for Fuel Type)

    EditText edit_start;        // Edit-Text for Starting Location input
    EditText edit_end;          // Edit-Text for Destination input

    String str_Brand;           // String to set on the button (btn_carBrand) after Brand selection
    String str_Model;           // String to set on the button (btn_carModel) after Model selection
    String str_Year;            // String to set on the button (btn_carYear) after Year selection

    TripLocation  startLocation;    // Selected Stating Location
    TripLocation  endLocation;      // Selected End Locaiton ( Destination

    TripVehicle tripVehicle;        // Vehicle to be created with the properties (Brand,Model,Year)
    private BottomNavigationView bottomNavigation;

    /**
     * This function gets called on Activity Creation.
     * It takes care of assignment of the views.
     *
     * @param savedInstanceState    Saved State of this instance when getting back on top of the
     *                              stack (e.g. when returning from other activities)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CheapTripApp)getApplication()).setCurrentActivity(this);

        setContentView(R.layout.activity_main);
        assignViewObjects();
        tripVehicle = new TripVehicle();


        /*======================================
         * Set tripVehicle Mock (in debug mode)
         *======================================*/
        if (BuildConfig.DEBUG) {
            if(tripVehicle == null || tripVehicle.getBrand() == null || tripVehicle.getModel() == null) {

                btn_carBrand.setText("BMW");
                btn_carModel.setText("318i");
                btn_carYear.setText("1991");
                tripVehicle = new TripVehicle();
                tripVehicle.setBrand("BMW");
                tripVehicle.setModel("135i Manual 6-spd");
                tripVehicle.setYear("2010");
                tripVehicle.setFueltype(GasStationClient.FuelType.E5);
            }
        }
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

        if ( this.equals(currActivity))
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

    /**
     * Assigns the Views to the object of this activity
     */
    private void assignViewObjects(){
        /*===============================================
         * Assign Views
         *===============================================*/
        btn_carBrand = findViewById(R.id.btn_car_brand);
        btn_carModel = findViewById(R.id.btn_car_model);
        btn_carYear = findViewById(R.id.btn_car_year);
        spin_carFuel = findViewById(R.id.spin_fuel_type);

        edit_start = findViewById(R.id.edit_start);
        edit_end = findViewById(R.id.edit_destination);

        gauge = findViewById(R.id.tank_indicator);
        bottomNavigation = findViewById(R.id.bottomNavigationView);

        Navigation.setBottomNavigation(this,bottomNavigation);
        /*==============================================
         * Set Click Listener for Fuel Type
         *==============================================*/
        spin_carFuel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String strFuelType = (String) spin_carFuel.getSelectedItem();

                if(strFuelType.equals(GasStationClient.FuelType.DIESEL.getFuelType())){
                    tripVehicle.setFueltype(GasStationClient.FuelType.DIESEL);
                }

                if(strFuelType.equals(GasStationClient.FuelType.E5.getFuelType())){
                    tripVehicle.setFueltype(GasStationClient.FuelType.E5);
                }

                if(strFuelType.equals(GasStationClient.FuelType.E10.getFuelType())){
                    tripVehicle.setFueltype(GasStationClient.FuelType.E10);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    /**
     * This function is a Handler for all Views of activity_main.
     * Decision to have one centralized Click Handler to match onActivityResult.
     *
     * @param view Clicked View by User
     */
    public void viewClickHandler(View view){
        Intent intent;
        int requestCode;
        Bundle optionsBundle = null;    // For Transition


        // double tankPercent = (double)seek_tankContents.getProgress()/ (double)seek_tankContents.getMax();
        double tankPercent = (double)gauge.getProgress()/ 100;
        tripVehicle.setRemainFuelPercent(tankPercent);

        Bundle bundle = new Bundle();

        switch(view.getId()){
            /*===================================================================================
             * Clicked the BrandButton (sends the uninitiliazed tripvehicle
             *===================================================================================*/
            case R.id.btn_car_brand:
                intent = new Intent(this, VehicleBrandActivity.class);
                tripVehicle = new TripVehicle();
                bundle.putSerializable("vehicle",tripVehicle);
                intent.putExtras(bundle);
                requestCode = ACTIVITY_REQ_CODE_BRAND;
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                break;
            /*====================================================================================
             * Clicked the BrandButton
             *====================================================================================*/
            case R.id.btn_car_model:
                intent = new Intent(this, VehicleModelActivity.class);
                bundle.putSerializable("vehicle",tripVehicle);
                intent.putExtras(bundle);
                requestCode = ACTIVITY_REQ_CODE_MODEL;
                break;
            /*====================================================================================
             * Clicked the BrandButton
             *====================================================================================*/
            case R.id.btn_car_year:
                intent = new Intent(this, VehicleYearActivity.class);
                bundle.putSerializable("vehicle",tripVehicle);
                intent.putExtras(bundle);
                requestCode = 3;
                break;
            /*====================================================================================
             * Clicked the BrandButton
             *====================================================================================*/
            case R.id.edit_start:
                intent = new Intent(this, MapActivity.class);
                bundle.putSerializable("vehicle",tripVehicle);
                intent.putExtras(bundle);

                // Animations
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

                    Pair<View,String> pairImageMap = Pair.create(findViewById(R.id.tank_indicator),"image_to_map");
                    Pair<View,String> pairEditStart = Pair.create((View)edit_start,"edit_start");
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,pairEditStart,pairImageMap);
                    optionsBundle = options.toBundle();
                }

                requestCode = ACTIVITY_REQ_CODE_START;
                break;
            /*====================================================================================
             * Clicked the BrandButton
             *====================================================================================*/
            case R.id.edit_destination:
                intent = new Intent(this, MapActivity.class);
                intent.putExtra("end", endLocation);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

                    Pair<View,String> pairImageMap = Pair.create(findViewById(R.id.tank_indicator),"image_to_map");
                    Pair<View,String> pairEditStart = Pair.create((View)edit_end,"edit_end");
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,pairEditStart,pairImageMap);
                    optionsBundle = options.toBundle();
                }

                requestCode = ACTIVITY_REQ_CODE_END;
                break;
            /*====================================================================================
             * Clicked the BrandButton
             *====================================================================================*/
            case R.id.btn_find:

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

                    Pair<View,String> pairImageMap = Pair.create(findViewById(R.id.tank_indicator),"rel_layout_calc_container");
                    Pair<View,String> pairEditStart = Pair.create(view,"viewpager_calc");
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,pairEditStart,pairImageMap);
                    optionsBundle = options.toBundle();
                }


                boolean bIsIncomplete = false;
                String toastText = "The following Porperties must be set:\n";
                if(startLocation == null){
                    toastText += " - Start Location\n";
                }
                if(endLocation == null){
                    toastText += " - Destination/ End Location\n";
                    bIsIncomplete = true;
                }

                if(tripVehicle.getBrand() == null){
                    toastText += " - Brand of the Vehicle\n";
                    bIsIncomplete = true;
                }

                if(tripVehicle.getModel() == null){
                    toastText += " - Model of the Vehicle\n";
                    bIsIncomplete = true;
                }

                if (bIsIncomplete == true){
                    Toast.makeText(this, toastText,Toast.LENGTH_LONG).show();
                    return;
                }
                intent = new Intent(this, CalculationActivity.class);
                intent.putExtra("start", startLocation);
                intent.putExtra("end", endLocation);
                intent.putExtra("tripVehicle",tripVehicle);
                requestCode = ACTIVITY_REQ_CODE_CALC;
                break;
            /*=========================================================
             * Nothing to do
             *=========================================================*/
            default:                        return;

        }
        /*=========================================================
         * Start the Activity
         *=========================================================*/
        startActivityForResult(intent, requestCode, optionsBundle);
    }

    /**
     * This is a callback function which will be called on returning from other activities
     * The other activity, which will be calling this method gets identified by request code.
     *
     * @param requestCode   Identifier of the activity which returns to the MainActivity
     * @param resultCode    The result code of the calling activity
     * @param data          Data send from calling activity to this
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK){
            return;
        }

        switch (requestCode){
            /*================================================================================================
             * Return from CarBrandActivity
             *================================================================================================*/
            case ACTIVITY_REQ_CODE_BRAND:       tripVehicle = (TripVehicle)data.getSerializableExtra("vehicle");
                str_Brand = tripVehicle.getBrand();
                btn_carBrand.setText(str_Brand);
                btn_carModel.setEnabled(true);
                break;
            /*================================================================================================
             * Return from CarModelActivity
             *================================================================================================*/
            case ACTIVITY_REQ_CODE_MODEL:       tripVehicle = (TripVehicle)data.getSerializableExtra("vehicle");
                str_Model = tripVehicle.getModel();
                btn_carModel.setText(str_Model);
                btn_carYear.setEnabled(true);
                break;
            /*================================================================================================
             * Return from CarYearActivity
             *================================================================================================*/
            case ACTIVITY_REQ_CODE_YEAR:        tripVehicle = (TripVehicle)data.getSerializableExtra("vehicle");
                str_Year = tripVehicle.getYear();
                btn_carYear.setText(str_Year);
                spin_carFuel.setEnabled(true);
                setFuelSpinner(tripVehicle);
                break;
            /*================================================================================================
             * Return from MapActivity for the Starting Location
             *================================================================================================*/
            case ACTIVITY_REQ_CODE_START:       startLocation = (TripLocation) data.getSerializableExtra("Location");
                String strStart = startLocation.getLocationName();
                edit_start.setText(strStart);
                break;
            /*================================================================================================
             * Return from MapActivity for the End Location/Destination
             *================================================================================================*/
            case ACTIVITY_REQ_CODE_END:         endLocation = (TripLocation) data.getSerializableExtra("Location");
                String strEnd = endLocation.getLocationName();
                edit_end.setText(strEnd);
                break;
            /*================================================================================================
             * No known activity
             *================================================================================================*/
            default:    break;
        }
        updateVehicleButtons(tripVehicle);
    }

    /**
     * This function will be called after Asking for specific App permissions.
     * In this case the only app permission asked for is GPS-Permission(identified by requestCode 4711)
     *
     * @param requestCode   RequestCode of the Permission request
     * @param permissions   The permission the user gets asked for
     * @param grantResults  The result of the request (users acceptance or rejection)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 4711) {

            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Showing the toast message
                Toast.makeText(MainActivity.this,"Location Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MainActivity.this,"Location Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }

    }

    /**
     * This sets the Spinner by getting the fuel type for specific tripVehicle
     * (filled properties: brand, model , year)
     *
     * @param tripVehicle   TripVehicle with populated Entries:
     *                          * Car Brand
     *                          * Car Model
     *                          * Car Year
     */
    private void setFuelSpinner(TripVehicle tripVehicle){
        List<GasStationClient.FuelType> fuelTypeList = determineFuelType(tripVehicle);

        List<String> choices = new ArrayList<>();

        for(GasStationClient.FuelType fuelType : fuelTypeList){
            choices.add(fuelType.getFuelType());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,R.layout.spinner_item_fueltype,choices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spin_carFuel.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    /**
     * This function determines the Fuel types for a given TripVehicle,
     * by given Make, Model and construction year.
     *
     * This is done by getting the fuel types from existent consumption entries of the database
     * for these specific values.
     *
     * @param tripVehicle   TripVehicle with set Make, Model and construction year
     * @return              List of available fuel Types for this TripVehicle
     */
    private List<GasStationClient.FuelType> determineFuelType(TripVehicle tripVehicle){
        List<GasStationClient.FuelType> fuelTypeList = new ArrayList<>();

        VehicleDatabaseClient dbClient = VehicleDatabase.getDatabase(this).vehicleDatabaseClient();

        String brand = tripVehicle.getBrand();
        String model = tripVehicle.getModel();
        String year = tripVehicle.getYear();


        List<String> fuelTypes = dbClient.getFuelTypes(brand,model,year);

        for(String fuelType : fuelTypes) {

            if(fuelType.equals(GasStationClient.FuelType.E5.getFuelType())){
                fuelTypeList.add(GasStationClient.FuelType.E5);
            }

            if(fuelType.equals(GasStationClient.FuelType.E10.getFuelType())){
                fuelTypeList.add(GasStationClient.FuelType.E10);
            }

            if(fuelType.equals(GasStationClient.FuelType.DIESEL.getFuelType())){
                fuelTypeList.add(GasStationClient.FuelType.DIESEL);
            }

        }
        return fuelTypeList;
    }


    private void updateVehicleButtons(TripVehicle tripVehicle){
        if(tripVehicle == null){
            btn_carBrand.setText(R.string.car_brand);

            btn_carModel.setText(R.string.car_model);
            btn_carModel.setEnabled(false);

            btn_carYear.setText(R.string.car_year);
            btn_carYear.setEnabled(false);
            return;
        }

        if(tripVehicle.getBrand() == null){
            btn_carBrand.setText(R.string.car_brand);

            btn_carModel.setText(R.string.car_model);
            btn_carModel.setEnabled(false);

            btn_carYear.setText(R.string.car_year);
            btn_carYear.setEnabled(false);
        }

        if(tripVehicle.getModel() == null){
            btn_carModel.setText(R.string.car_model);

            btn_carYear.setText(R.string.car_year);
            btn_carYear.setEnabled(false);
        }

        if(tripVehicle.getYear() == null){
            btn_carYear.setText(R.string.car_year);

            spin_carFuel.setAdapter(new ArrayAdapter<>(this,R.layout.spinner_item_fueltype));
        }
    }
}
