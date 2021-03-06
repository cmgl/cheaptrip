package com.example.cheaptrip.dao.rest;

import com.example.cheaptrip.handlers.converter.annotations.Json;
import com.example.cheaptrip.models.nhtsa.VehicleBrandResponse;
import com.example.cheaptrip.models.nhtsa.VehicleModelResponse;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CarSpecClient {
      //https://vpic.nhtsa.dot.gov/api/vehicles/getallmakes?format=json
    @Json
    @GET("getallmakes")
    Call<VehicleBrandResponse> getAllBrands(@Query("format") String format);

    @Json
    @GET("GetMakesForVehicleType/{type}")
    Call<VehicleBrandResponse> getBrands(@Path("type") String vehicleType, @Query("format") String format);

    @Json
    @GET("GetModelsForMake/{brand}")
    Call<VehicleModelResponse> getModels(@Path("brand") String vehicleBrand, @Query("format") String format);
}
