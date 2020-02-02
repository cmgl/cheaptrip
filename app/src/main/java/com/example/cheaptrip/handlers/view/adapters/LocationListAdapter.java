package com.example.cheaptrip.handlers.view.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.cheaptrip.R;

import com.example.cheaptrip.models.photon.Location;

import java.util.List;

public class LocationListAdapter extends BaseAdapter {
    private Context context;


    private List<Location> mDisplayedLocations;    // Values to be displayed

    public LocationListAdapter(@NonNull Context context, List<Location> locationList) {
        //super(context, R.layout.list_row_vehicle_selection);
        this.mDisplayedLocations = locationList;

        this.context = context;
    }

    @Override
    public int getCount() {
        return mDisplayedLocations.size();
    }

    @Override
    public Object getItem(int position) {
        return mDisplayedLocations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return System.identityHashCode(mDisplayedLocations.get(position));
    }



    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.list_row_vehicle_selection, parent, false);
        }
        TextView textView = (TextView) row.findViewById(R.id.listText);
        Location location = mDisplayedLocations.get(position);

        String locationName = location.getStringForList();

        textView.setText(locationName);

        return row;
    }




}
