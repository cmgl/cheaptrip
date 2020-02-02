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
import com.example.cheaptrip.models.nhtsa.VehicleModel;

import java.util.ArrayList;

import java.util.Comparator;
import java.util.List;
public class VehicleModelAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private List<VehicleModel> vehicleModelList;

    private List<VehicleModel> mDisplayedModels;    // Values to be displayed

    public VehicleModelAdapter(@NonNull Context context, List<VehicleModel> vehicleModelList) {
        //super(context, R.layout.list_row_vehicle_selection);
        sortVehicleModels(vehicleModelList);
        this.vehicleModelList = vehicleModelList;
        this.mDisplayedModels = vehicleModelList;

        this.context = context;
    }

    @Override
    public int getCount() {
        return mDisplayedModels.size();
    }

    @Override
    public VehicleModel getItem(int position) {
        return mDisplayedModels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mDisplayedModels.get(position).getModelID();
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
        VehicleModel list = mDisplayedModels.get(position);
        String model= list.getModelName();
        textView.setText(model);

        return row;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mDisplayedModels = (List<VehicleModel>) results.values; // has the filtered values
                sortVehicleModels(mDisplayedModels);
                notifyDataSetChanged();  // notifies the data with new filtered values
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                List<VehicleModel> FilteredArrList = new ArrayList<VehicleModel>();

                if (mDisplayedModels == null) {
                    mDisplayedModels =  new ArrayList<VehicleModel>(mDisplayedModels); // saves the original data in mOriginalValues
                }

                /********
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 ********/
                if (constraint == null || constraint.length() == 0) {

                    // set the Original result to return
                    results.count = vehicleModelList.size();
                    results.values = vehicleModelList;

                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < vehicleModelList.size(); i++) {
                        String data = vehicleModelList.get(i).getModelName();
                        if (data.toLowerCase().startsWith(constraint.toString())) {
                            FilteredArrList.add(vehicleModelList.get(i));
                        }
                    }

                    sortVehicleModels(FilteredArrList);

                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;


                }
                return results;
            }
        };
        return filter;
    }


    private List<VehicleModel> sortVehicleModels(List<VehicleModel> modelList){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            modelList.sort(new Comparator<VehicleModel>() {
                @Override
                public int compare(VehicleModel model1, VehicleModel model2) {
                    String model1_name = model1.getModelName().trim();
                    String model2_name = model2.getModelName().trim();

                    return model1_name.compareTo(model2_name);
                }
            });
        }

        return modelList;
    }
}
