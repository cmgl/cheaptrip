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
import com.example.cheaptrip.models.nhtsa.VehicleBrand;

import java.util.ArrayList;

import java.util.Comparator;
import java.util.List;
public class VehicleBrandAdapter extends BaseAdapter implements Filterable {

    private Context context;
    private List<VehicleBrand> vehicleBrandList;
    private List<VehicleBrand> mDisplayedBrands;    // Values to be displayed


    public VehicleBrandAdapter(@NonNull Context context) {
        this.context = context;
        vehicleBrandList = new ArrayList<>();
        mDisplayedBrands = new ArrayList<>();
    }

    public VehicleBrandAdapter(@NonNull Context context, List<VehicleBrand> vehicleBrandList) {
        setList(vehicleBrandList);
        this.context = context;
    }

    public void setList(List<VehicleBrand> vehicleBrandList){
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
        return mDisplayedBrands.get(position).getMakeId();
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
        VehicleBrand list = mDisplayedBrands.get(position);
        String brand= list.getMakeName();
        textView.setText(brand);

        return row;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mDisplayedBrands = (List<VehicleBrand>) results.values; // has the filtered values
                sortVehicleBrands(mDisplayedBrands);
                notifyDataSetChanged();  // notifies the data with new filtered values
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                List<VehicleBrand> FilteredArrList = new ArrayList<VehicleBrand>();

                if (mDisplayedBrands == null) {
                    mDisplayedBrands =  new ArrayList<VehicleBrand>(mDisplayedBrands); // saves the original data in mOriginalValues
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
                        String data = vehicleBrandList.get(i).getMakeName();
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


    private List<VehicleBrand> sortVehicleBrands(List<VehicleBrand> brandList){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            brandList.sort(new Comparator<VehicleBrand>() {
                @Override
                public int compare(VehicleBrand brand1, VehicleBrand brand2) {
                    String brand1_name = brand1.getMakeName().trim();
                    String brand2_name = brand2.getMakeName().trim();

                    return brand1_name.compareTo(brand2_name);
                }
            });
        }

        return brandList;
    }
}
