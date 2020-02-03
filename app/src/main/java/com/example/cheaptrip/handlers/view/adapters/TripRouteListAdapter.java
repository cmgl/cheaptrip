package com.example.cheaptrip.handlers.view.adapters;


import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.cheaptrip.R;
import com.example.cheaptrip.models.TripRoute;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class TripRouteListAdapter extends BaseAdapter {
    private List<TripRoute> tripRouteList;
    private Context context;

    public TripRouteListAdapter(@NonNull Context context){
        this.context = context;
        tripRouteList = new ArrayList<>();
    }

    public TripRouteListAdapter( Context context, List<TripRoute> tripRouteList) {
        this.context = context;
        this.tripRouteList = tripRouteList;

        if(tripRouteList != null){
            sortForCosts();
        }
    }

    @Override
    public int getCount() {
        if(tripRouteList != null) {
            return tripRouteList.size();
        }
        return 0;
    }

    @Override
    public TripRoute getItem(int position) {
        if(tripRouteList != null){
            return tripRouteList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        if(tripRouteList != null) {
            return System.identityHashCode(tripRouteList.get(position));
        }
        return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.list_row_routes, parent, false);
        }

        TextView textView = (TextView) row.findViewById(R.id.tv_route_properties);
        TripRoute route = tripRouteList.get(position);

        double costs = route.getCosts();
        double duration = route.getDuration();
        double distance = route.getDistance();

        int hours = (int)duration/3600;
        int mins =  (int)(duration%3600)/60;
        int secs =  (int)(duration%3600)%60;

        String strTime = "";
        if(hours > 0)
            strTime += hours + " h ";

        if(hours > 0 && mins > 0)
            strTime += mins + " m";

        strTime += secs + " s";

        String text = String.format(Locale.GERMANY,"%23.2f €\n%22s\n%20.2f km",costs, strTime, distance);

        textView.setText(text);

        return row;
    }

    public List<TripRoute> getTripRouteList() {
        return tripRouteList;
    }

    public void setTripRouteList(List<TripRoute> tripRouteList) {
        this.tripRouteList = tripRouteList;
        if(tripRouteList != null){
            sortForCosts();
        }
    }

    /**
     * Sorts the list in place by costs
     *
     * @return
     */
    public void sortForCosts(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tripRouteList.sort(new Comparator<TripRoute>() {
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
            tripRouteList.sort(new Comparator<TripRoute>() {
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
            tripRouteList.sort(new Comparator<TripRoute>() {
                @Override
                public int compare(TripRoute route1, TripRoute route2) {
                    return Double.compare(route1.getDuration(),route2.getDuration());
                }
            });
        }
        notifyDataSetChanged();

    }
}
