package com.example.cheaptrip.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.LocalActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cheaptrip.R;
import com.example.cheaptrip.app.CheapTripApp;
import com.example.cheaptrip.handlers.view.LocationListHandler;
import com.example.cheaptrip.handlers.rest.RestListener;
import com.example.cheaptrip.handlers.rest.geo.GeoLocationsForNameHandler;
import com.example.cheaptrip.handlers.rest.geo.GeoNameForLocationHandler;
import com.example.cheaptrip.models.TripLocation;
import com.example.cheaptrip.models.photon.Location;
import com.example.cheaptrip.models.photon.Properties;
import com.example.cheaptrip.services.GPSService;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;


import java.util.ArrayList;
import java.util.List;

public class MapActivity extends Activity {

    private EditText editLocationInput;                 // Text field to search for Locations

    private MapView mMapView = null;                    // View, representing the map
    private IMapController mMapController = null;       // Controller for the map

    private TripLocation tripLocation;                  // This will be the result of the activity
                                                        // (user will select a tripLocation)

    private TextView txtView_currentLocation;

    private ListView locationList;                      // Location list to be shown on user input
                                                        // on the text field

    private Marker currentSelectedMarker;               // Marker, representing the current selected Location
    private Marker currentLocationMarker;               // Marker, representing the current current Location
    private Marker customLocationMarker;                // Marker, representing a Location picked form the map


    private volatile boolean bFinished = false;          // Indicator to show wheter activity is destroyed
                                                        // as consequence all running async tasks get cancelled

    /**
     * Enum, to identify the purpose of a Marker
     * This enum will be used to set marker icons
     */
    public enum MARKERTYPE{
        CURRENT,    // Current Location (location of this device)
        SELECTED,   // Custom selected Location from Map
        LOADED      // Loaded Locations from Search
    }

    /**
     * This function will be called on Activity Creation.
     *
     * It initializes the views, attached to the Layout and sets current Properties:
     *      *   it initializes the Map
     *      *   it initializes a hidden list, which will be populated when the user types something
     *          into the Edit-Text field editLocationInput
     *      *   it sets a TouchListener on the Map to handle Location
     *      *   sets the current location as a marker
     *
     * @param savedInstanceState    the state of the Activity when it has been opened before
     */
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CheapTripApp)getApplication()).setCurrentActivity(this);
        //handle permissions first, before mMapView is created. not depicted here

        //load/initialize the osmdroid configuration
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        /*===============================================================================
         * inflate and create the Views
         *==============================================================================*/
        setContentView(R.layout.activity_map);
        mMapView = findViewById(R.id.map);
        editLocationInput = findViewById(R.id.edit_start);
        locationList = findViewById(R.id.list_map_locations);
        txtView_currentLocation = findViewById(R.id.tv_curr_location);
        /*===============================================================================
         * Initialize the Map and the Location List
         *==============================================================================*/
        initMap();
        initList();
        setTouchListener();
        setCurrentLocationMarker();

        editLocationInput.requestFocus();   // Request Focus (to start writing to the Edit text)
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        bFinished = true;

        CheapTripApp cheapTripApp = (CheapTripApp) getApplication();
        Activity currActivity = cheapTripApp.getCurrentActivity() ;

        if ( this .equals(currActivity))
            cheapTripApp.setCurrentActivity( null ) ;
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        mMapView.onResume(); //needed for compass, my location overlays, v6.0.0 and up

        CheapTripApp cheapTripApp = (CheapTripApp) getApplication();
        cheapTripApp .setCurrentActivity( this ) ;
    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        mMapView.onPause();  //needed for compass, my location overlays, v6.0.0 and up

        CheapTripApp cheapTripApp = (CheapTripApp) getApplication();
        Activity currActivity = cheapTripApp.getCurrentActivity() ;

        if ( this .equals(currActivity))
            cheapTripApp.setCurrentActivity( null ) ;
    }

    private void initMap(){
        mMapView.setMultiTouchControls(true);
        /*===============================================================================
         * Set different Tile Source (--> HttpMapnik is faster as the default TileSource)
         *==============================================================================*/
        //mMapView.setTileSource(TileSourceFactory.MAPNIK);
        mMapView.setTileSource(
                new XYTileSource("HttpMapnik",
                        0, 19, 256, ".png", new String[] {
                        "http://a.tile.openstreetmap.org/",
                        "http://b.tile.openstreetmap.org/",
                        "http://c.tile.openstreetmap.org/" },
                        "© OpenStreetMap contributors")
        );
        /*===============================================================================
         * Get the current Location
         *==============================================================================*/
        GPSService gpsService = new GPSService(this);

        if (!gpsService.canGetLocation()) {
            Toast.makeText(this,"Could not determine your Location",Toast.LENGTH_LONG).show();
            Log.e("initMap()","GPS-Service could not get Location");
            return;
        }

        final double latitude = gpsService.getLatitude();
        final double longitude = gpsService.getLongitude();
        /*===============================================================================
         * Center Map on current Location
         *==============================================================================*/
        mMapController = mMapView.getController();
        mMapController.setZoom(20.0);
        GeoPoint startPoint = new GeoPoint(latitude, longitude);
        mMapController.setCenter(startPoint);

    }

    /**
     * Initializes the list of locations to be shown on user input.
     * It takes care of the Input of the Edit-Text to populate the location List.
     */
    private void initList() {
        GPSService gpsService = new GPSService(this);

        if (gpsService.canGetLocation()) {
            final double latitude = gpsService.getLatitude();       // current latitude
            final double longitude = gpsService.getLongitude();     // current longitude

            /*=============================================================================
             * Initialize LocationListHandler
             * ( this will take care of transforming user input into a list of locations)
             *=============================================================================*/
            LocationListHandler locationListHandler = new LocationListHandler(editLocationInput, locationList,latitude,longitude);
            locationListHandler.setInputListener();
            locationListHandler.setListTouchListeners();
            locationListHandler.setEditorActionListener();
        }

    }

    /**
     * This functions sets a TouchListener on the map,
     * so the user can select custom locations from it.
     */
    private void setTouchListener(){
        Log.i("CHEAPTRIP", "Applying TouchListeners to Map Activity");
        /*=======================================================================
         * Set on touch hide keyboard
         *=======================================================================*/
        mMapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(mMapView.getWindowToken(), 0);
                MapActivity.super.onTouchEvent(event);
                mMapView.invalidate();
                return false;
            }
        });

        /*========================================================================
         * Set the touchListener to apply a marker to that custom Location
         *=======================================================================*/
        final MapEventsReceiver mReceive = new MapEventsReceiver(){

            @Override
            public boolean singleTapConfirmedHelper(GeoPoint selectedGeoPoint) {

                final double latitude = selectedGeoPoint.getLatitude();
                final double longitude = selectedGeoPoint.getLongitude();

                tripLocation = new TripLocation(latitude,longitude);
                setMarker(tripLocation,MARKERTYPE.SELECTED,false);

                return true;
            }
            @Override
            public boolean longPressHelper(GeoPoint p) {
                Log.e("CHEAPTRIP", "Could not get the ");
                return false;
            }

        };
        mMapView.getOverlays().add(new MapEventsOverlay(mReceive));
    }

    /**
     * This functions purpose is to set the marker for the current location of this device.
     */
    private void setCurrentLocationMarker(){
        Log.i("CHEAPTRIP", "Setting Marker for current Location (location of this device");
        /*===============================================================================
         * Get the current Location
         *==============================================================================*/
        GPSService gpsService = new GPSService(this);

        if (!gpsService.canGetLocation()) {
            Toast.makeText(this,"Could not determine your Location",Toast.LENGTH_LONG).show();
            Log.e("initMap()","GPS-Service could not get Location");
            return;
        }

        final double latitude = gpsService.getLatitude();
        final double longitude = gpsService.getLongitude();
        /*===============================================================================
         * Create the marker
         *==============================================================================*/
        TripLocation tripLocation = new TripLocation(latitude,longitude);
        setMarker(tripLocation, MARKERTYPE.CURRENT,true);
    }

    /**
     * updateCurrentMarker() will handle the selection of a specific marker (provided by argument.
     *      * If the marker is the same as the current location, it will remove the previous selection.
     *
     *      * If there is no previous selection (marker) it will close the info window of the marker,
     *        representing current location and will set the selected Marker
     *        to the marker provided by the argument
     *
     *      * if the current selection is the same as a custom selected location from the map,
     *        it will remove the previous selected marker from the map.
     *
     *      * Else, the previous selected marker is a marker loaded from the text-input
     *        and has to be kept on the map. It will only close the info window of the previous marker.
     *
     * @param marker    marker to be set as current selection
     */
    public void updateCurrentMarker(Marker marker){
        Log.d("CHEAPTRIP","Updating current Selected Marker (updating drawable)");
        if(marker == null){
            Log.e("updateCurrentMarker()","Current Selected Marker is null");
            return;
        }
        /*==============================================================
         * Marker is current location -> remove current selected marker
         ===============================================================*/
        if(marker == currentLocationMarker){
            currentSelectedMarker.getInfoWindow().close();
            mMapView.getOverlays().remove(currentSelectedMarker);
            currentLocationMarker.showInfoWindow();
            //currentSelectedMarker = currentLocationMarker;
            return;
        }
        /*==============================================================
         * No marker selection --> set current selected marker
         ===============================================================*/
        if(currentSelectedMarker == null){
            currentLocationMarker.getInfoWindow().close();
            currentSelectedMarker = marker;
            currentSelectedMarker.showInfoWindow();
            return;
        }
        /*========================================================================
         * custom pick from map is the current selected marker --> can be removed
         =========================================================================*/
        if(currentSelectedMarker == customLocationMarker){
            currentLocationMarker.getInfoWindow().close();
            currentSelectedMarker.getInfoWindow().close();
            mMapView.getOverlays().remove(currentSelectedMarker);
            currentSelectedMarker = marker;
            return;
        }

        /*==================================================================================
         * if persistent marker --> change previous selected marker (icon)
         ===================================================================================*/
        if(currentSelectedMarker == currentLocationMarker){
            currentLocationMarker.setIcon(getResources().getDrawable(R.drawable.person));
        }else{
            currentSelectedMarker.setIcon(getResources().getDrawable(R.drawable.osm_ic_center_map));
        }

        /*================================================================================================
         * Default Action --> close previously opened InfoWindow and set this marker as current Selection
         *===============================================================================================*/
        currentSelectedMarker.getInfoWindow().close();
        currentLocationMarker.getInfoWindow().close();


        if(mMapView.getOverlays().contains(marker)) {
            mMapView.getOverlays().remove(marker);
            mMapView.getOverlays().add(marker);
        }

        currentSelectedMarker = marker;
        currentSelectedMarker.setIcon(getResources().getDrawable(R.drawable.marker_default));

        currentSelectedMarker.showInfoWindow();

        mMapView.invalidate();
    }

    /**
     * A marker will be added to the map with the location, provided by argument.
     *
     * The markers icon will be set based on the argument <markertype>:
     *      * CURRENT:      current location of this device
     *      * SELECTED:     Selection by the user (either from laoded markers or picked from the map)
     *      * LOADED:       Loaded by typing in the search field.
     *
     * @param location      location of the marker to be set
     * @param markertype    type of the marker
     * @param center        indicates, whether the map as to be centered around the new marker
     */
    public void setMarker(TripLocation location, MARKERTYPE markertype, boolean center){
        Log.d("CHEAPTRIP", "setMarker(): Setting Marker for a Location");

        if(location == null){
            Log.d("setMarker()","Cannot set Marker: Location may not be null");
            return;
        }

        Double latitude = location.getLatitdue();
        Double longitude = location.getLongitude();
        /*====================================================
         * Close current Selection
         *====================================================*/
        if(currentSelectedMarker != null) {
            currentSelectedMarker.getInfoWindow().close();
        }
        /*====================================================
         * Initialize the Marker
         *====================================================*/
        Marker marker = new Marker(mMapView);
        marker.setPosition(new GeoPoint(latitude,longitude));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Loading Location Name...");


        marker.setInfoWindow(new TripInfoWindow("Loading Location Name...", mMapView));
        marker.showInfoWindow();
        /*===============================================================
         * Set the icon based on the Purpose
         *      * CURRENT:      shows current location
         *      * SELECTED:     Current Selection
         *      * LOADED:       Individual Marker, loaded from REST-Api
         *===============================================================*/
        Drawable markerIcon;

        switch (markertype){
            case CURRENT:   markerIcon = getResources().getDrawable(R.drawable.person);
                            currentLocationMarker = marker;
                            currentSelectedMarker = marker;
                            mMapView.getController().setZoom(20.0);
                            break;

            case SELECTED:  markerIcon = getResources().getDrawable(R.drawable.marker_default);
                            updateCurrentMarker(marker);
                            customLocationMarker = marker;
                            currentSelectedMarker = marker;
                            break;

            case LOADED:    markerIcon = getResources().getDrawable(R.drawable.osm_ic_center_map);
                            currentSelectedMarker = marker;
                            mMapView.getController().setZoom(20.0);
                            marker.getInfoWindow().close();
                            break;
            default:
                throw new IllegalStateException("Unexpected value: " + markertype);
        }

        marker.setIcon(markerIcon);

        String title = location.getInfoWindowText();

        if(title == null || title.length() <= 0) {
            loadLocationTitle(marker);
        }else{
            marker.setTitle(title);
            TripInfoWindow tripInfoWindow = (TripInfoWindow) marker.getInfoWindow();
            tripInfoWindow.setText(title);
        }


        mMapView.getOverlays().add(marker);
        mMapView.invalidate();

        if(center){
            mMapView.getController().setCenter(new GeoPoint(latitude,longitude));
        }

        setMarkerClickListener(marker);

    }

    /**
     *  This function loads the Location Attributes (City, Street, Postcode ...) for a specific marker.
     *  This will be shown in the Info-Window of the marker.
     *
     *  By calling the Rest-API of Photon it will return a String containing the relevant location
     *  information for a specific Coordinate (the marker's coordinate)
     *
     * @param marker    Marker, to be updated with the location text
     */
    private void loadLocationTitle(final Marker marker){
        Log.d("CHEAPTRIP","loadLocationTitle(): Getting location title for a marker");
        if(marker == null){
            Log.e("CHEAPTRIP","loadLocationTitle(): Cannot determine location Name when Marker is null");
            return;
        }

        final GeoNameForLocationHandler geoNameListRestHandler = new GeoNameForLocationHandler(marker.getPosition().getLatitude(),marker.getPosition().getLongitude());

        geoNameListRestHandler.makeAsyncRequest(new RestListener<TripLocation>() {
            @Override
            public void OnRestSuccess(final TripLocation tripLocation) {
                Activity currentActivity = ((CheapTripApp)getApplication()).getCurrentActivity();

                String title = tripLocation.getInfoWindowText();

                if(currentActivity instanceof MapActivity){

                    Log.d("CHEAPTRIP", "Success: Getting location name for Marker");

                    marker.setTitle(title);

                    TripInfoWindow tripInfoWindow = (TripInfoWindow) marker.getInfoWindow();

                    if(tripInfoWindow != null) {
                        tripInfoWindow.setText(title);
                    }else{
                        Log.e("CHEAPTRIP","loadLocationTitle: InfoWindow is null");
                    }
                }else if (currentActivity instanceof MainActivity){
                    ((MainActivity) currentActivity).edit_start.setText(title);
                }else{
                    Log.e("CHEAPTRIP", "Failed to set location Name: Activity not provided");
                }

            }

            @Override
            public void OnRestFail() {
                Activity currentActivity = ((CheapTripApp)getApplication()).getCurrentActivity();

                Log.e("CHEAPTRIP","Success: Could not determine Location name for Marker");
                String locationName = "Could not load LocationName";

                if(currentActivity instanceof MapActivity){
                    marker.setTitle(locationName);
                    TripInfoWindow tripInfoWindow = (TripInfoWindow) marker.getInfoWindow();
                    tripInfoWindow.setText(locationName);

                }else if (currentActivity instanceof MainActivity){
                    ((MainActivity) currentActivity).edit_start.setText(locationName);
                }else{
                    Log.e("CHEAPTRIP", "Failed to set location Name: Activity not provided");
                }
            }
        });
    }

    public void loadMarkers(List<TripLocation> locationList){
        if (locationList == null || locationList.size() <= 0){
            return;
        }

        for(TripLocation location : locationList){
            setMarker(location, MARKERTYPE.LOADED,true);
        }

        if (locationList.size() > 1) {
            BoundingBox boundingBox = determineBoundingBox(locationList);
            mMapView.zoomToBoundingBox(boundingBox, true,150);

        }else if (locationList.size() == 1 ){
            double lon = locationList.get(0).getLongitude();
            double lat = locationList.get(0).getLatitdue();

            mMapController.setCenter(new GeoPoint(lat,lon));
            mMapController.setZoom(18.0);
        }else{
            Log.w("CHEAPTRIP","location List is empty.");
        }

    }

    /**
     * This function sets Markers, identified by a specific location name, to the map
     * (The location name provided by Location-Search)
     *
     * This is done by calling the Rest-API of Photon, which will result for a List of Locations
     * with a given location name.
     *
     * @param locationName
     */
    public void loadMarkers(final String locationName){
        final GeoLocationsForNameHandler geoLocationsForNameHandler = new GeoLocationsForNameHandler(locationName);

        geoLocationsForNameHandler.makeAsyncRequest(new RestListener< List<Location>>() {

            @Override
            public void OnRestSuccess( List<Location> locationList) {

                if (locationList == null || locationList.size() <= 0){
                    return;
                }

                List<TripLocation> tripLocationList = new ArrayList<>();

                for(Location location : locationList){
                    TripLocation tripLocation = new TripLocation(location);
                    tripLocationList.add(tripLocation);
                    setMarker(tripLocation,MARKERTYPE.LOADED,false);
                }

                if (locationList.size() > 1) {
                    BoundingBox boundingBox = determineBoundingBox(tripLocationList);
                    mMapView.zoomToBoundingBox(boundingBox, true,150);
                }else if (locationList.size() == 1 ){
                    double lon = locationList.get(0).getCoordinates().get(0);
                    double lat = locationList.get(0).getCoordinates().get(1);

                    mMapController.setCenter(new GeoPoint(lat,lon));
                }else{
                    Log.w("CHEAPTRIP","location List is empty.");
                }
            }

            @Override
            public void OnRestFail() {
                Context context = getApplicationContext();
                Toast.makeText(context,"Could not load Positions",Toast.LENGTH_LONG);
            }
        });
    }

    private static String generateMarkerLabel(Location location){
        Properties properties = location.getProperties();

        /*====================================================
         * Extract Values
         *====================================================*/
        String city = properties.getCity();
        if (city == null){
            city = "";
        }

        String locationName = properties.getName();
        if (locationName == null){
            locationName = "";
        }

        String street = properties.getStreet();

        if (street == null){
            street = "";
        }

        String housenumber = properties.getHousenumber();
        if (housenumber == null){
            housenumber = "";
        }
        final String labelText = locationName + "\nCity: " + city + "\nStreet: " + street + " " + housenumber;

        return labelText;
    }

    public void setMarkerClickListener(Marker marker){


        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, final MapView mapView) {

                if (marker != null){
                    double latitude = marker.getPosition().getLatitude();
                    double longitude = marker.getPosition().getLongitude();
                    String locationName = marker.getTitle();

                    tripLocation = new TripLocation(locationName,latitude,longitude);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("Location",tripLocation);

                    setResult(Activity.RESULT_OK,intent);

                    TripInfoWindow tripInfoWindow = (TripInfoWindow)marker.getInfoWindow();
                    tripInfoWindow.setText(locationName);

                    updateCurrentMarker(marker);

                    //txtView_currentLocation.setText(locationName);
                }

                mMapView.invalidate();
                return true;
            }
        });
    }

    public void onLocationSelected(View view){

        double latitude = currentSelectedMarker.getPosition().getLatitude();
        double longitude = currentSelectedMarker.getPosition().getLongitude();
        String locationName = currentSelectedMarker.getTitle();

        tripLocation = new TripLocation(locationName,latitude,longitude);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("Location",tripLocation);

        setResult(Activity.RESULT_OK,intent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition();
        }else{
            finish();
        }
    }


    private static BoundingBox determineBoundingBox(List<TripLocation> locationList){

        if (locationList == null || locationList.size() <= 0){
            Log.e("CHEAPTRIP", "Cannot Determine BoundingBox for Empty or noninitialized location list.");
            return null;
        }

        Double minLon = locationList.get(0).getLongitude();
        Double maxLon = minLon;

        Double minLat = locationList.get(0).getLatitdue();
        Double maxLat = minLat;

        for(TripLocation location : locationList) {

            Double longitude = location.getLongitude();
            Double latitude = location.getLatitdue();

            if (latitude < minLat) {
                minLat = latitude;
            }
            if (latitude > maxLat) {
                maxLat = latitude;
            }

            if (longitude < minLon) {
                minLon = longitude;
            }

            if (longitude > maxLon) {
                maxLon = longitude;
            }
        }

        return new BoundingBox(maxLat, maxLon, minLat, minLon);
    }

    private class TripInfoWindow extends InfoWindow{

        private TextView tvLocation;
        public TripInfoWindow(String locationText, MapView mapView) {
            super(R.layout.info_window, mapView);
            tvLocation = super.mView.findViewById(R.id.tv_curr_location);
            tvLocation.setText(locationText);

        }

        public void setText(String locationName){

            tvLocation.setText(locationName);
            tvLocation.invalidate();

            // Workaround for wrapping Content (Fitting the box around)
            int visibility = tvLocation.getVisibility();
            tvLocation.setVisibility(View.GONE);
            tvLocation.setVisibility(visibility);

            visibility = mView.getVisibility();
            mView.setVisibility(View.GONE);
            mView.setVisibility(visibility);

        }
        @Override
        public void onOpen(Object item) {

        }

        @Override
        public void onClose() {

        }
    }
}