package com.example.cheaptrip.handlers.view.adapters;


import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.cheaptrip.R;
import com.example.cheaptrip.models.TripGasStation;
import com.example.cheaptrip.models.TripLocation;
import com.example.cheaptrip.models.TripRoute;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class GasStationListAdapter extends BaseAdapter {
    private Context context;


    private List<TripRoute> tripStationRoutes;    // Values to be displayed

    /**
     * Default constructor
     *
     *  @param context           Context from which this Adapter is used
     */
    public GasStationListAdapter(Context context){
        this.context = context;
        tripStationRoutes = new ArrayList<>();
    }


    /**
     * Constructor
     *
     * @param context           Context from which this Adapter is used
     * @param tripStationRoutes A list for this adapter
     */
    public GasStationListAdapter(@NonNull Context context, List<TripRoute> tripStationRoutes) {
        this.tripStationRoutes = tripStationRoutes;

        //super(context, R.layout.list_row_vehicle_selection);
        if(tripStationRoutes != null){
            sortForDistance();
        }
        //this.tripStationRoutes = tripStationRoutes;
        this.context = context;
    }

    @Override
    public int getCount() {
        return tripStationRoutes.size();
    }

    @Override
    public Object getItem(int position) {
        return tripStationRoutes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return System.identityHashCode(tripStationRoutes.get(position));
    }


    public void setList(List<TripRoute> tripStationRoutes){
        this.tripStationRoutes = tripStationRoutes;
        if(tripStationRoutes != null){
            sortForDistance();
        }else {
            Log.w("CHEAPTRIP","GasStationListAdapter->setList(): Cannot set list: provided list is null");
        }
    }

    public List<TripRoute> getList(){
        return tripStationRoutes;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.list_row_trip_gas_station, parent, false);
        }

        TextView tvBrand =  row.findViewById(R.id.tv_gasstation_brand);
        TextView tvName =  row.findViewById(R.id.tv_gasstation_name);
        TextView tvRouteInfo = row.findViewById(R.id.tv_route_properties);

        TripRoute tripRoute = tripStationRoutes.get(position);
        /*=============================================================
         * Set GasStation Info
         *=============================================================*/
        List<TripLocation> locations = tripRoute.getStops();

        if(locations == null || locations.isEmpty()){
            Log.w("CHEAPTRIP","GasStationListAdapter(): Route has no Locations");
            return row;
        }

        // Last Location of the Route must be the Station
        TripLocation tripLocation = locations.get(locations.size()-1);

        if(!(tripLocation instanceof TripGasStation)) {
            Log.e("CHEAPTRIP", "GasStationListAdapter: Last location of the route is not a station");
            return row;
        }

        TripGasStation tripGasStation = (TripGasStation) tripLocation;

        String street = tripGasStation.getStreet();
        String houseNumber = tripGasStation.getHousenumber();
        String brand = tripGasStation.getBrand();

        String locationName = tripGasStation.getName();

        tvBrand.setText(brand);
        tvName.setText(locationName);
        /*=============================================================
         * Set Route Info
         *=============================================================*/
        double distance = tripRoute.getDistance();
        double duration = tripRoute.getDuration();

        /*===================================
         * Set Distance
         *=================================*/
        DecimalFormat decimalFormat = new DecimalFormat(".#");
        String strDistance = "";

        if(distance < 3){
            distance = distance * 1000;
            strDistance = (int) distance + " m";
        }else{
            strDistance = decimalFormat.format(distance) + " km";
        }
        /*===================================
         * Set Duration
         *=================================*/
        double hours = duration/3600;
        double mins =  (hours - (int)hours )*60;
        double  secs =  (mins - (int)mins) * 60;

        String strTime = "";

        if((int)hours != 0){
            strTime += (int)hours + " h ";
        }

        if((int)mins != 0){
            strTime += (int)mins + " m ";
        }

        strTime += (int)secs + " s ";

        tvRouteInfo.setText(strDistance + "\n" + strTime);

        return row;
    }

    /**
     * Sorts the list in place by costs
     *
     * @return
     */
    public void sortForCosts(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tripStationRoutes.sort(new Comparator<TripRoute>() {
                @Override
                public int compare(TripRoute route1, TripRoute route2) {
                    return Double.compare(route1.getCosts(),route2.getCosts());
                }
            });
        }

        notifyDataSetChanged();
    }

    public void sortForDistance(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tripStationRoutes.sort(new Comparator<TripRoute>() {
                @Override
                public int compare(TripRoute route1, TripRoute route2) {
                    return Double.compare(route1.getDistance(),route2.getDistance());
                }
            });
        }
        notifyDataSetChanged();
    }

    public void sortForDuration(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tripStationRoutes.sort(new Comparator<TripRoute>() {
                @Override
                public int compare(TripRoute route1, TripRoute route2) {
                    return Double.compare(route1.getDuration(),route2.getDuration());
                }
            });
        }
        notifyDataSetChanged();

    }



}
