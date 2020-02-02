package com.example.cheaptrip.handlers.view.adapters;


import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.cheaptrip.R;

import java.util.ArrayList;

import java.util.Comparator;
import java.util.List;
public class SelectionListAdapter extends BaseAdapter implements Filterable {

    private Context context;
    private List<String> vehicleBrandList;
    private List<String> mDisplayedBrands;    // Values to be displayed


    public SelectionListAdapter(@NonNull Context context) {
        this.context = context;
        vehicleBrandList = new ArrayList<>();
        mDisplayedBrands = new ArrayList<>();
    }

    public SelectionListAdapter(@NonNull Context context, List<String> vehicleBrandList) {
        setList(vehicleBrandList);
        this.context = context;
    }

    public void setList(List<String> vehicleBrandList){
        sortVehicleBrands(vehicleBrandList);
        this.vehicleBrandList = vehicleBrandList;
        this.mDisplayedBrands = vehicleBrandList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDisplayedBrands.size();
    }

    @Override
    public Object getItem(int position) {
        return mDisplayedBrands.get(position);
    }

    @Override
    public long getItemId(int position) {
        return System.identityHashCode(mDisplayedBrands.get(position));
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
        String brand = mDisplayedBrands.get(position);
        textView.setText(brand);

        return row;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mDisplayedBrands = (List<String>) results.values; // has the filtered values
                sortVehicleBrands(mDisplayedBrands);
                notifyDataSetChanged();  // notifies the data with new filtered values
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                List<String> FilteredArrList = new ArrayList<String>();

                if (mDisplayedBrands == null) {
                    mDisplayedBrands =  new ArrayList<String>(mDisplayedBrands); // saves the original data in mOriginalValues
                }

                /********
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 ********/
                if (constraint == null || constraint.length() == 0) {

                    // set the Original result to return
                    results.count = vehicleBrandList.size();
                    results.values = vehicleBrandList;

                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < vehicleBrandList.size(); i++) {
                        String data = vehicleBrandList.get(i);
                        if (data.toLowerCase().startsWith(constraint.toString())) {
                            FilteredArrList.add(vehicleBrandList.get(i));
                        }
                    }

                    sortVehicleBrands(FilteredArrList);

                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;


                }
                return results;
            }
        };
        return filter;
    }


    private List<String> sortVehicleBrands(List<String> brandList){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            brandList.sort(new Comparator<String>() {
                @Override
                public int compare(String brand1, String brand2) {
                    String brand1_name = brand1.trim();
                    String brand2_name = brand2.trim();

                    return brand1_name.compareTo(brand2_name);
                }
            });
        }

        return brandList;
    }
}
