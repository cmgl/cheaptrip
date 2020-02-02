package com.example.cheaptrip.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.viewpager.widget.ViewPager;

import com.example.cheaptrip.R;
import com.example.cheaptrip.app.CheapTripApp;
import com.example.cheaptrip.handlers.rest.RestListener;
import com.example.cheaptrip.handlers.rest.geo.GeoDirectionsHandler;
import com.example.cheaptrip.views.Gauge;
import com.example.cheaptrip.views.Navigation;
import com.example.cheaptrip.views.fragments.CalcGasStationFragment;
import com.example.cheaptrip.views.fragments.CalcMapFragment;
import com.example.cheaptrip.views.fragments.CalcRouteFragment;
import com.example.cheaptrip.handlers.CalculationListener;

import com.example.cheaptrip.handlers.view.adapters.CalcViewPagerAdapter;


import com.example.cheaptrip.handlers.view.adapters.TripRouteListAdapter;
import com.example.cheaptrip.models.TripGasStation;
import com.example.cheaptrip.models.TripLocation;
import com.example.cheaptrip.models.TripRoute;
import com.example.cheaptrip.models.TripVehicle;

import com.example.cheaptrip.services.RouteService;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;


import java.util.List;


public class CalculationActivity extends AppCompatActivity {

    private ListView lvRoutes;                              // List view for the TripRoutes
    private TripRouteListAdapter tripRouteListAdapter;      // Adapter for the List of TripRoutes

    private Gauge progressBar;                        // Progress bar to be shown on load of
    // the list entries

    TripLocation startLocation;                             // Starting Location
    TripLocation endLocation;                               // End Location (Destination)

    TripVehicle tripVehicle;                                // Vehicle to be used for Calculation

    private CalcGasStationFragment mStationFragment;        // Fragment of the tab for GasStation info
    private CalcMapFragment mMapFragment;                   // Fragment of the tab for Map info
    private CalcRouteFragment mRouteFragment;               // Fragment of the tab for Route info

    private ViewPager mViewPager;                           // The View pager for the fragments

    private volatile boolean mIsListLoaded = false;         // will be set as soon the TripRoute List is loaded
    private BottomNavigationView bottomNavigation;

    /**
     * This function will be called on Activity creation
     * It takes care of initializing the views attached to the layout
     * and starts calculating routes with a gas station as intermediate stop
     *
     * @param savedInstanceState    Bundle, containing the state of the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CheapTripApp)getApplication()).setCurrentActivity(this);
        /*============================================================
         * Init the Views
         *============================================================*/
        setContentView(R.layout.activity_calculation);
        progressBar = findViewById(R.id.progress_gauge);
        lvRoutes = findViewById(R.id.list_routes);
        mViewPager = findViewById(R.id.viewpager_calc);

        bottomNavigation = findViewById(R.id.bottomNavigationView);
        bottomNavigation.setSelectedItemId(R.id.bottom_nav_stations);
        Navigation.setBottomNavigation(this,bottomNavigation);

        /*============================================================
         * Get Data from Intent
         *============================================================*/
        startLocation = (TripLocation) getIntent().getSerializableExtra("start");
        endLocation = (TripLocation) getIntent().getSerializableExtra("end");

        tripVehicle = (TripVehicle) getIntent().getSerializableExtra("tripVehicle");
        /*============================================================
         * Start Actions
         *============================================================*/
        initFragments();
        initList();
        startCalculation();
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

    /**
     * Initializes the Fragments for
     *      * Gas station details
     *      * Map details
     *      * Route details ( steps to be taken for navigation)
     */
    private void initFragments(){
        mMapFragment = new CalcMapFragment();
        mStationFragment = new CalcGasStationFragment();
        mRouteFragment = new CalcRouteFragment();


        CalcViewPagerAdapter calcViewPagerAdapter = new CalcViewPagerAdapter(getSupportFragmentManager());
        calcViewPagerAdapter.addFragment(mStationFragment,"Station");
        calcViewPagerAdapter.addFragment(mMapFragment,"Map");
        calcViewPagerAdapter.addFragment(mRouteFragment,"Route");

        mViewPager.setAdapter(calcViewPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tab_layout_calc);

        mViewPager.setCurrentItem(1);

        tabLayout.setupWithViewPager(mViewPager);

        animateTankIndicator();
    }

    /**
     * Initializes the List of Calculated routes.
     *
     * Sets a ItemClicked Listener updating the fragments for
     *          * GasStation details ({@link CalcGasStationFragment}
     *          * Map details   ({@link CalcGasStationFragment}
     *          * Route details ({@link CalcRouteFragment}
     *
     * 1) The GasStation details will be loaded from the list item (pre-initialized)
     * 2) the Map Details will be loaded either
     *          * from the item if initialized before
     *          * per Rest-API request (getting the geoJSON to be drawn to the map)
     * 3) the
     */
    private void initList(){
        tripRouteListAdapter = new TripRouteListAdapter(this);
        lvRoutes.setAdapter(tripRouteListAdapter);

        lvRoutes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mMapFragment.clearMap();

                final TripRoute oldTripRoute = (TripRoute)lvRoutes.getItemAtPosition(position);

                /*=====================================================================
                 * Set the Fragments if Item (TripRoute) already contains the GeoJSON
                 * (set by previous API-Request
                 *=====================================================================*/
                if(oldTripRoute.getGeoJSON() != null){
                    fillFragments(oldTripRoute);
                    return;
                }
                /*=====================================================================
                 * Get the GeoJSON for setting the Route to the MapFragment
                 * (not already in ListItem (= TripRoute) )
                 *=====================================================================*/
                final GeoDirectionsHandler geoDirectionsHandler = new GeoDirectionsHandler(oldTripRoute.getStops(),oldTripRoute);

                geoDirectionsHandler.makeAsyncRequest(new RestListener<TripRoute>() {
                    /*================================================
                     * REST SUCCESS -> Fill Fragments
                     *================================================*/
                    @Override
                    public void OnRestSuccess(TripRoute tripRoute) {
                        List<TripRoute> tripRouteList = tripRouteListAdapter.getTripRouteList();
                        int index = tripRouteList.indexOf(oldTripRoute);

                        tripRouteList.set(index,tripRoute);

                        tripRouteListAdapter.setTripRouteList(tripRouteList);
                        tripRouteListAdapter.notifyDataSetChanged();

                        fillFragments(tripRoute);
                    }
                    /*================================================
                     * REST FAIL -> Log error
                     *================================================*/
                    @Override
                    public void OnRestFail() {
                        Log.e("CHEAPTRIP","CalculationActivity-> initList(): Could not set GeoJSON to MapFragment");
                    }
                });

            }
        });
    }

    /**
     * This will be called to get the Lists of Routes with weights:
     *      * Costs
     *      * Duration
     *      * distance
     *
     * This is done by calling a AsyncTask which callbacks the Inner function calculationDone()
     * with parameter tripRouteList, containing all Routes with the previous mentioned weights.
     *
     * The List Adapter of the ListView will be set and notified for data change
     */
    private void startCalculation(){
        RouteService routeService = new RouteService(this, tripVehicle, new CalculationListener() {
            @Override
            public void onCalculationDone(List<TripRoute> tripRouteList) {
                tripRouteListAdapter.setTripRouteList(tripRouteList);
                tripRouteListAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.INVISIBLE);
                mIsListLoaded = true;
            }
        });

        routeService.execute(startLocation, endLocation);

    }


    /**
     * Animates the Tank Gauge until the load is finished (mIsListLoaded = true)
     */
    private void animateTankIndicator(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!mIsListLoaded) {
                        // Load from 0% to 100%
                        for (int i = 0; i <= 100; i++) {
                            progressBar.setProgress(i);
                            Thread.sleep(5);
                        }

                        // Load from 100% to 0%
                        for (int i = 100; i >= 0; i--) {
                            progressBar.setProgress(i);
                            Thread.sleep(5);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }
    /**
     * This function will set the details of the fragments
     *      * {@link CalcGasStationFragment}
     *      * {@link CalcMapFragment}
     *      * {@link CalcRouteFragment}
     *
     * for the selection of the TripRoute tripRoute (provided by Arguments
     *
     * @param tripRoute     Selected TripRoute from the list lvRoutes.
     */
    private void fillFragments(TripRoute tripRoute){
        mMapFragment.updateMap(tripRoute,true);

        for(TripLocation tripLocation : tripRoute.getStops()){
            if(tripLocation instanceof TripGasStation){
                mStationFragment.setGasStationInfo((TripGasStation)tripLocation);
            }
        }

        mRouteFragment.updateList(tripRoute);
    }

    /**
     * Callback function for the filter buttons.
     * Sorts the list items  by duration, costs and distance
     * depending on the button that was clicked.
     *
     * @param view  Filter button that was clicked by user
     */
    public void onSortButtonClicked(View view) {
        if(tripRouteListAdapter == null){
            Log.e("CHEAPTRIP","Cannot sort list: gasStationListAdapter is null");                return;
        }

        switch (view.getId()){
            case R.id.btn_filter_costs:
                tripRouteListAdapter.sortForCosts();
                break;

            case R.id.btn_filter_distance:
                tripRouteListAdapter.sortForDistance();
                break;

            case R.id.btn_filter_duration:
                tripRouteListAdapter.sortForDuration();
                break;

            default: break;
        }
    }
}
