package com.example.cheaptrip.handlers.rest.vehicle;

import android.util.Log;
import android.widget.Toast;

import com.example.cheaptrip.dao.CarSpecClient;
import com.example.cheaptrip.dao.VehicleDataClient;
import com.example.cheaptrip.handlers.rest.RestHandler;
import com.example.cheaptrip.handlers.rest.RestListener;
import com.example.cheaptrip.models.fueleconomy.VehicleDataSet;
import com.example.cheaptrip.models.nhtsa.VehicleBrand;
import com.example.cheaptrip.models.nhtsa.VehicleBrandResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Response;

public class VehicleDataSetHandler extends RestHandler<List<VehicleDataSet>,String> {
    private static final String BASE_URL = "http://data.cheaptrip.cf/";
    private static VehicleDataClient vehicleDataClient;

    /**
     * Constructor: Initializes Retrofit and creates an Client for CarSpecClient Class
     */
    public VehicleDataSetHandler(){
        super(BASE_URL);
        vehicleDataClient = super.getRetrofit().create(VehicleDataClient.class);

        Call call = vehicleDataClient.getVehicleData();

        super.setCall(call);
    }

    /**
     * Extracts the relevant Data from the Rest-Response (List of Car Brands.
     *
     * @param response Response from Rest-api of webservice (NHTSA).
     * @return List of VehicleBrands (POJO-Objects) holding names for Car Brands.
     */
    @Override
    public List<VehicleDataSet> extractDataFromResponse(Response<String> response) {
        String jsonResponse = response.body();

        LinkedTreeMap root = getTreeMap(jsonResponse);
        LinkedTreeMap data = (LinkedTreeMap) root.get("data");

        return generateVehicleDataSet(data);
    }

    /**
     * Start asynchronious Rest Call. An Instance of RestListener has to be passed.
     * The RestListener's functions OnRestSuccess or OnRestFail will be called when response is received
     * or an error occured.
     *
     * @param restListener Object implementing RestListener.
     *                     Its functions will be called when request is finished.
     */
    public void startGetVehicleData(final RestListener restListener) {
        assert(vehicleDataClient != null);
        super.makeAsyncRequest(restListener);
    }


    private static LinkedTreeMap getTreeMap(String jsonContent) {
        LinkedTreeMap o = null;
        try {

            JsonReader jsonReader = new JsonReader(new StringReader(jsonContent));
            Gson gson = new GsonBuilder().create();
            jsonReader.beginArray();
            int numberOfRecords = 0;

            while (jsonReader.hasNext()) {
                o = gson.fromJson(jsonReader, Object.class);
                numberOfRecords++;
            }
            jsonReader.endArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }

    private List<VehicleDataSet> generateVehicleDataSet(LinkedTreeMap treeMap){
        List<VehicleDataSet> vehicleDataSetList = new ArrayList<>();

        if(treeMap == null || treeMap.size() <= 0){
            Log.e("CHEAPTRIP","generateVehicleDataSet(): cannot generate List<VehicleDataSet> from empty treeMap");
            return null;
        }

        try {

            Set<String> yearSet = getYears(treeMap);

            for (String year : yearSet) {
                Set<String> brandSet = getBrands(treeMap, year);

                for(String brand : brandSet){
                    Set<String> modelSet = getModels(treeMap,year,brand);

                    for(String model : modelSet){
                        Set<String> fuelTypeSet = getFuelTypes(treeMap,year,brand,model);

                        Double regular = null;
                        Double premium = null;
                        Double diesel = null;

                        for(String fuelType : fuelTypeSet){
                            double consumption = getConsumption(treeMap,year,brand,model,fuelType);


                            if (model.compareTo("325i")== 0 ){
                                int a = 0;
                                a++;
                            }
                            if(fuelType.compareTo("Diesel") == 0){
                                diesel = consumption;
                            }

                            if(fuelType.compareTo("Regular") == 0){
                                regular = consumption;
                            }

                            if (fuelType.compareTo("Premium") == 0){
                                premium = consumption;
                            }
                        }

                        VehicleDataSet vehicleDataSet = new VehicleDataSet(year,brand,model,regular,premium,diesel);
                        vehicleDataSetList.add(vehicleDataSet);
                    }

            }

            }
        }catch(Exception e){
            Log.e("CHEAPTRIP","generateVehicleDataSet(): Parsing Error. Could not load treeMap into a List of VehicleDataSets: " + e.getMessage());
            return null;
        }

        return vehicleDataSetList;
    }


    private static Set<String> getYears(LinkedTreeMap data){
        /*if (!(year instanceof String)){
            Log.e("CHEAPTRIP","generateVehicleDataSet(): Parsing Error. Year not in format String.");
            return null;
        }*/
        return data.keySet();
    }

    private static Set<String> getBrands(LinkedTreeMap data, String year){
        LinkedTreeMap result = (LinkedTreeMap) data.get(year);
        return result.keySet();
    }

    private static Set<String> getModels(LinkedTreeMap data, String year, String brand){
        LinkedTreeMap result = (LinkedTreeMap) data.get(year);
        LinkedTreeMap result2 = (LinkedTreeMap) result.get(brand);
        return result2.keySet();
    }

    private static Set<String> getFuelTypes(LinkedTreeMap data, String year, String brand, String model){
        LinkedTreeMap result = (LinkedTreeMap) data.get(year);
        LinkedTreeMap result2 = (LinkedTreeMap) result.get(brand);
        LinkedTreeMap result3 = (LinkedTreeMap) result2.get(model);
        return result3.keySet();
    }

    private static Double getConsumption(LinkedTreeMap data, String year, String brand, String model, String fuelType){
        LinkedTreeMap result = (LinkedTreeMap) data.get(year);
        LinkedTreeMap result2 = (LinkedTreeMap) result.get(brand);
        LinkedTreeMap result3 = (LinkedTreeMap) result2.get(model);
        Double result4 = (Double) result3.get(fuelType);
        return result4;
    }
}
