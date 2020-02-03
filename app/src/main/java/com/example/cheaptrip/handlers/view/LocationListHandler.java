package com.example.cheaptrip.handlers.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;

import android.widget.ListView;
import android.widget.TextView;

import com.example.cheaptrip.R;
import com.example.cheaptrip.activities.MapActivity;
import com.example.cheaptrip.handlers.rest.RestListener;
import com.example.cheaptrip.handlers.rest.geo.GeoLocationsForNameHandler;
import com.example.cheaptrip.handlers.view.adapters.LocationListAdapter;
import com.example.cheaptrip.models.TripLocation;
import com.example.cheaptrip.models.photon.Location;

import java.util.List;


/**
 * This class handles input in the search box (EditText) in {@link MapActivity}.
 *
 * It calls the rest api of photon to get AutoCompleted GeoNames from the Webservice
 * and passes them to a list on Success.
 *
 * It also takes care of setting multiple markers to the map when the user "hits" search.
 * Then it shows all marker that could be relevant by createria entered location name.
 *
 * It also sets a listener on the provided listview (see constructor).
 * When an item of the list is clicked it will set a marker to the Map. (single location selection)
 *
 */
public class LocationListHandler {
    private final Animation animSlideDown;
    private final Animation animSlideUp;
    private final Animation animZoomIn;

    private EditText locationInput;
    private ListView listView;
    private double lat;
    private double lon;
    private static Context context;

    private volatile boolean bItemSelected = false; //Indicator if an Item is selected

    GeoLocationsForNameHandler mGeoNameRestHandler;


    /**
     * Constructor
     * @param locationInput     TextView which represents a search box for Locations
     * @param listView          ListView to be populated by the results of the search
     * @param lat               Based on this Latitude nearby locationNames will appear first
     * @param lon               Based on this Longitude nearby locationNames will appear first
     */
    public LocationListHandler(EditText locationInput, final ListView listView, double lat, double lon) {
        this.locationInput = locationInput;
        this.listView = listView;
        this.lat = lat;
        this.lon = lon;

        this.context = listView.getContext();

        animSlideDown = AnimationUtils.loadAnimation(context, R.anim.slide_down);
        animSlideUp = AnimationUtils.loadAnimation(context, R.anim.slide_up);
        animZoomIn = AnimationUtils.loadAnimation(context, R.anim.zoom_in);
    }


    public void setEditorActionListener(){
        locationInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String inputText = v.getText().toString();

                    if(context instanceof MapActivity){
                        MapActivity mapActivity = (MapActivity)context;
                        mapActivity.loadMarkers(inputText);
                        listView.startAnimation(animSlideUp);
                    }
                }
                return true;
            }
        });
    }

    public void setInputListener() {
        locationInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, final int count) {

                if (!bItemSelected) {
                    if (listView != null) {

                        if(mGeoNameRestHandler != null){
                            mGeoNameRestHandler.cancel();
                        }

                        mGeoNameRestHandler = new GeoLocationsForNameHandler(s.toString(), lat, lon);
                        mGeoNameRestHandler.makeAsyncRequest(new RestListener<List<Location>>() {
                            @Override
                            public void OnRestSuccess(List<Location> locations) {

                                if (locations == null || locations.size() < 1) {
                                    Log.w("CHEAPTRIP", "Received locationList is null or empty.");
                                    return;
                                }

                                LocationListAdapter locationListAdapter = new LocationListAdapter(context, locations);
                                locationListAdapter.notifyDataSetChanged();
                                listView.setAdapter(locationListAdapter);
                                //listView.startAnimation(animSlideDown);

                            }

                            @Override
                            public void OnRestFail() {
                                Log.e("CHEAPTRIP", "Error during Request for Locations (Photon)");
                            }
                        });
                    }
                }
                bItemSelected = false;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void setListTouchListeners() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Location location = (Location) parent.getItemAtPosition(position);

                bItemSelected = true;
                locationInput.setText(location.getStringForList());
                view.startAnimation(animZoomIn);
                listView.startAnimation(animSlideUp);


                ViewGroup.LayoutParams layoutParams = listView.getLayoutParams();
                layoutParams.height = 0;

                listView.setLayoutParams(layoutParams);
                MapActivity mapActivity = (MapActivity) context;

                TripLocation tripLocation = new TripLocation(location);
                mapActivity.setMarker(tripLocation, MapActivity.MARKERTYPE.SELECTED, true);
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                InputMethodManager in = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(listView.getWindowToken(), 0);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }
}
