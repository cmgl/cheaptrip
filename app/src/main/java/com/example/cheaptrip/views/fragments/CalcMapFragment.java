package com.example.cheaptrip.views.fragments;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.cheaptrip.R;
import com.example.cheaptrip.activities.MapActivity;
import com.example.cheaptrip.handlers.rest.RestListener;
import com.example.cheaptrip.handlers.rest.geo.GeoDirectionsHandler;
import com.example.cheaptrip.handlers.view.MyKmlStyler;
import com.example.cheaptrip.models.TripGasStation;
import com.example.cheaptrip.models.TripLocation;
import com.example.cheaptrip.models.TripRoute;
import com.example.cheaptrip.models.TripVehicle;
import com.example.cheaptrip.models.orservice.ORServiceResponse;
import com.example.cheaptrip.models.tankerkoenig.Station;
import com.example.cheaptrip.services.GPSService;
import com.google.gson.Gson;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the Fragment of {@link com.example.cheaptrip.activities.CalculationActivity}
 * (on tap Map).
 *
 * It displays a map holding the information of the current Route and the Route Selected from
 * the Activity's list of Routes ( with gas station as intermediate stop).
 *
 * It displays the route and the markers (stops) of the trip.
 */
public class CalcMapFragment extends Fragment {
    private static MapView  mMapView = null;
    private static IMapController mMapController = null;

    private TripRoute mTripRoute;

    private String startEndRouteJSON;

    private TripLocation startLocation;
    private TripLocation endLocation;

    TripVehicle tripVehicle;                            // TripVehicle



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_calc_map, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);

        /*============================================================
         * Init the Views
         *============================================================*/
        startLocation = (TripLocation) getActivity().getIntent().getSerializableExtra("start");
        endLocation = (TripLocation) getActivity().getIntent().getSerializableExtra("end");

        tripVehicle = (TripVehicle) getActivity().getIntent().getSerializableExtra("tripVehicle");

        mMapView = view.findViewById(R.id.mapView);
        /*================================================================
         * Init the Map and get
         *=================================================================*/
        initMap();
        /*================================================================
         * Draw the current Selected Route
         *=================================================================*/
        if(startLocation != null || endLocation != null) {
            getBaseRoute(startLocation, endLocation);

            if (mTripRoute != null) {
                updateMap(mTripRoute, true);
            }
        }
    }

    public void onResume() {
        super.onResume();
        mMapView.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause() {
        super.onPause();
        mMapView.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    public void clearMap(){
        if(mMapView == null){
            Log.e("Cheaptrip", "Cannot clear Map. mMapView is null");
            return;
        }

        mMapView.getOverlays().clear();
    }

    private void initMap(){
        mMapView.setTileSource(TileSourceFactory.MAPNIK);
        mMapView.setMultiTouchControls(true);

        GPSService gpsService = new GPSService(getActivity().getApplicationContext());

        if(startLocation == null || endLocation == null){
            return;
        }

        if (gpsService.canGetLocation()) {

            mMapController = mMapView.getController();
            mMapController.setZoom(20.0);
            GeoPoint startPoint = new GeoPoint(startLocation.getLatitdue(), startLocation.getLongitude());
            mMapController.setCenter(startPoint);

            drawMarker(startLocation);
            drawMarker(endLocation);

        }

    }

    private void getBaseRoute(TripLocation startLocation, TripLocation endLocation){
        if(startLocation == null){
            Log.e("CHEAPTRIP","CalcMapFragment->getBaseRoute: Cannot get the base Route: startLocation is null");
            return;
        }

        if(endLocation == null){
            Log.e("CHEAPTRIP","CalcMapFragment->getBaseRoute: Cannot get the base Route: endLocation is null");
            return;
        }

        List<TripLocation> tripLocationList = new ArrayList<>();
        tripLocationList.add(startLocation);
        tripLocationList.add(endLocation);

        GeoDirectionsHandler geoDirectionsHandler = new GeoDirectionsHandler(tripLocationList,null);

        geoDirectionsHandler.makeAsyncRequest(new RestListener<TripRoute>() {
            @Override
            public void OnRestSuccess(TripRoute tripRoute) {
                if(tripRoute == null){
                    Log.e("CHEAPTRIP","Received null as ORService response.");
                    return;
                }

                String geoJSON = tripRoute.getGeoJSON();
                startEndRouteJSON = geoJSON;
                drawRoute(geoJSON, Color.GREEN);
                setDirectionBbox(geoJSON);
            }

            @Override
            public void OnRestFail() {
                Log.e("CHEAPTRIP","Error getting Directions from OpenRoute Service.");

            }
        });
    }


    private void drawMarker(TripLocation tripLocation){
        Marker marker= new Marker(mMapView);
        marker.setPosition(new GeoPoint(tripLocation.getLatitdue(),tripLocation.getLongitude()));
        marker.setTitle(tripLocation.getLocationName());



        Drawable icon;
        Drawable defaultIcon = getResources().getDrawable(R.drawable.marker_default);
        if(tripLocation instanceof TripGasStation){

            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_gas_station_map, null);
            //icon = getResources().getDrawable(R.drawable.ic_gas_station);
            //icon.setBounds(0,0,defaultIcon.getIntrinsicWidth(), defaultIcon.getIntrinsicHeight());
            icon.setBounds(icon.getBounds());

        }else{
            icon = defaultIcon;
        }

        marker.setIcon(icon);
        marker.setAnchor(0.3f, 0.3f);
        mMapView.getOverlays().add(marker);
        mMapView.invalidate();
    }

    public void drawRoute(String geoJSON, int color){
        if (geoJSON != null && geoJSON.length() > 0) {
            try {
                KmlDocument kmlDocument = new KmlDocument();
                kmlDocument.parseGeoJSON(geoJSON);
                KmlFeature.Styler styler = new MyKmlStyler(color);
                FolderOverlay myOverLay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(mMapView, null, styler, kmlDocument);
                mMapView.getOverlays().add(myOverLay);
                mMapView.invalidate();


                setDirectionBbox(geoJSON);
            }catch(IllegalStateException e){
                Log.e("CHEAPTRIP","Cannot draw Route from GeoJSON: " +  e.getLocalizedMessage());
                return;
            }
        }
    }

    public void setDirectionBbox(String geoJSON){
        if(geoJSON == null || geoJSON.length() < 1){
            Log.e("CHEAPTRIP", "Cannot set BoundingBox from Empty GeoJSON String");
            return;
        }

        Gson gson = new Gson();

        ORServiceResponse orServiceResponse = gson.fromJson(geoJSON,ORServiceResponse.class);


        List<Double> responseBbox = orServiceResponse.getBbox();

        BoundingBox bbox =  new BoundingBox(
                responseBbox.get(3),
                responseBbox.get(2),
                responseBbox.get(1),
                responseBbox.get(0)
        );
        mMapView.zoomToBoundingBox(bbox,true,150);

    }

    public void drawRange(TripLocation centeredLocation, final double radius) {
        /*-------------------------------------------------------------------------
         * Draw the a circle for the max Radius to reach with current tank contents
         *-------------------------------------------------------------------------*/
        drawMarker(startLocation);
        ArrayList<OverlayItem> overlayItems = new ArrayList<>();
        OverlayItem overlayCircle = new OverlayItem("Radius", null, new GeoPoint(centeredLocation.getLatitdue(), centeredLocation.getLongitude()));
        overlayItems.add(overlayCircle);

        GeoPoint startIGeoPoint = new GeoPoint(centeredLocation.getLatitdue(), centeredLocation.getLongitude());

        double radiusInMeters = radius * 1000;
        List<GeoPoint> circle = Polygon.pointsAsCircle(startIGeoPoint, radiusInMeters);

        Polygon polygon = new Polygon();
        polygon.setPoints(circle);

        mMapView.getOverlays().add(polygon);
        mMapView.invalidate();
    }


    public void drawStations(List<Station> stations){
        /*--------------------------------------------------------
         * Set the Markers for the Gas Stations
         *--------------------------------------------------------*/
        for (Station station : stations){

            Marker stationMarker = new Marker(mMapView);

            stationMarker.setPosition(new GeoPoint(station.getLat(),station.getLng()));
            stationMarker.setTitle(station.getName() + "\nDiesel:" + station.getDiesel() + "\nE10" + station.getE10() + "\nE5:" + station.getE5());
            stationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            mMapView.getOverlays().add(stationMarker);
            mMapView.invalidate();
        }
    }

    private void updateCurrentRoute(String geoJSON,boolean updateBbox) {
        drawRoute(geoJSON, Color.GREEN);

        if(startEndRouteJSON != null){
            drawRoute(startEndRouteJSON, Color.BLACK);
        }

        if(updateBbox) {
            setDirectionBbox(geoJSON);
        }
    }

    private void updateMarkers(List<TripLocation> tripLocationList,boolean updateBbox) {
        for(TripLocation tripLocation : tripLocationList){
            drawMarker(tripLocation);
        }

        if(updateBbox) {
            BoundingBox boundingBox = MapActivity.determineBoundingBox(tripLocationList);

            mMapView.zoomToBoundingBox(boundingBox, true,150);
            mMapView.invalidate();
        }
    }

    public void updateMap(TripRoute tripRoute, boolean updateBbox){
        mTripRoute = tripRoute;
        String geoJSON = tripRoute.getGeoJSON();

        updateCurrentRoute(geoJSON,updateBbox);
        updateMarkers(tripRoute.getStops(),!updateBbox);


    }

    public void drawMarkersInRadius(List<TripLocation> tripLocationList, boolean updateBox){
        updateMarkers(tripLocationList,updateBox);
    }


}
