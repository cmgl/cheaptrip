package com.example.cheaptrip.activities;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.cheaptrip.R;
import com.example.cheaptrip.app.CheapTripApp;
import com.example.cheaptrip.dao.database.VehicleDatabaseClient;
import com.example.cheaptrip.database.VehicleDatabase;
import com.example.cheaptrip.handlers.view.adapters.SelectionListAdapter;
import com.example.cheaptrip.models.TripVehicle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * This Classes purpose is to provide all current Vehicle Brands as List,
 * so that the user can choose the relevant brand for further calculation.
 *
 * This is done by calling the web interface (REST-Api)
 * of the National Highway and Security Administration and populating the list for further interaction.
 *
 * As the user chooses a vehicle brand it will return to the calling activity whilst delivering the
 * selection (brand name) to it (as extra).
 */
public class VehicleBrandActivity extends ListActivity {

    private ProgressBar progressBar;                // Loading Icon until List is loaded (using REST-API)
    private EditText edit_searchBrand;              // EditText Field for Search

    private SelectionListAdapter listDataAdapter;    // List Adapter for VehicleBrands

    private TripVehicle tripVehicle;                // the TripVehicle to be filled

    /**
     * Gets Called when VehicleBrandActivity will be created.
     * Starts Asynchronious Call of the Webservice-Rest-API
     * @param savedInstanceState: State of this unique Instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CheapTripApp)getApplication()).setCurrentActivity(this);

        // Initialize Views
        setContentView(R.layout.activity_car_brand);
        progressBar = findViewById(R.id.progress_gauge);
        edit_searchBrand = findViewById(R.id.edit_search_vehicle_property);

        // Get tripVehicle
        Intent intent = getIntent();

        if(intent.hasExtra("vehicle")){
            tripVehicle = (TripVehicle)intent.getSerializableExtra("vehicle");
        }else{
            Log.e("CHEAPTRIP", "Vehicle not passed to VehicleBrandActivity");
            throw new IllegalStateException("Vehicle not passed to VehicleBrandActivity");
        }

        // Initialize ListAdapter
        listDataAdapter = new SelectionListAdapter(this);
        setListAdapter(listDataAdapter);
        setBrandListView();
        setTextChangedListener();
        assertMembersInitialized();
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
        overridePendingTransition(0, 0);
        CheapTripApp cheapTripApp = (CheapTripApp) getApplication();
        Activity currActivity = cheapTripApp.getCurrentActivity() ;

        if ( this .equals(currActivity))
            cheapTripApp.setCurrentActivity( null ) ;
    }

    /**
     * This is a Callback function, which gets triggered when an item gets clicked.
     * It will send the Item Title (=Brand) back to calling Activity ( MainActivity) and finishes.
     *
     * @param l:        the ListActivity's listView
     * @param v:        the listItem (as View)
     * @param position: the position of the clicked item in the List
     * @param id:       Identifier of the clicked Item
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        assertMembersInitialized();
        super.onListItemClick(l, v, position, id);
        /*----------------------------------------------------------
         * Get the Brand from the selected  item of the  ListView
         *---------------------------------------------------------*/
        String selectedBrand = (String) getListView().getItemAtPosition(position);
        tripVehicle.setBrand(selectedBrand);
        /*----------------------------------------------------------
         * Send the Brand Name back to MainActivity
         *----------------------------------------------------------*/
        Intent intent = new Intent(this, MainActivity.class);

        Bundle bundle = new Bundle();
        bundle.putSerializable("vehicle",tripVehicle);
        intent.putExtras(bundle);

        setResult(Activity.RESULT_OK,intent);

        finish();
    }

    /**
     * Populates the ListView by Calling the Rest-Interface of NHTSA and storing the response's
     * data as List to  listDataAdapter (Type: VehicleBrandAdapter).
     *
     * The response is received asynchroniously and removes the Progressbar.
     */
    public void setBrandListView() {
        assertMembersInitialized();

        List<String> brandNameList = getBrandNamesFromDatabase();
        listDataAdapter.setList(brandNameList);
        listDataAdapter.notifyDataSetChanged();

        assertMembersInitialized();
    }

    /**
     * Sets a Listener to the EditText-View.
     * When the text is changed a filter will be applied to ths List according to the entered Content
     */
    private void setTextChangedListener(){
        assertMembersInitialized();

        edit_searchBrand.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                listDataAdapter.getFilter().filter(s.toString());
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                listDataAdapter.getFilter().filter(s.toString());
            }
        });

        assertMembersInitialized();
    }

    /**
     * Gets all Brands from Database.
     * Filtered by the contents of the Trip-Vehicle.
     *
     * If the year of the Trip-Vehicle is set it will filter by year.
     * The same procedure for the model of the TripVehicle
     *
     * @return List of filtered or unfiltered Names of Brands (String)
     */
    private List<String> getBrandNamesFromDatabase(){

        VehicleDatabaseClient dbClient = VehicleDatabase.getDatabase(this).vehicleDatabaseClient();

        List<String> dataSetList = new ArrayList<>();

        if(tripVehicle == null){
            dataSetList = dbClient.getAllBrands();
        }

        String model = tripVehicle.getModel();
        String year = tripVehicle.getYear();

      /*  if(model != null && model.length() > 0){
            dataSetList = dbClient.getBrandsForModel(model);
        } else if(year != null && year.length() > 0){
            dataSetList = dbClient.getBrandsForYear(year);
        }else{
            dataSetList = dbClient.getAllBrands();
        }*/

        dataSetList = dbClient.getAllBrands();
        Collections.sort(dataSetList);

        return dataSetList;
    }

    /**
     * A Class Invariant.
     * Reassures that all the relevant Member Variables are initialized.
     */
    private void assertMembersInitialized(){
        assert (listDataAdapter != null);
        assert (edit_searchBrand != null);
        assert (progressBar != null);
    }
}
