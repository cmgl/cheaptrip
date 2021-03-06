package com.example.cheaptrip.dao.rest;


import com.example.cheaptrip.handlers.converter.annotations.Xml;
import com.example.cheaptrip.models.fueleconomy.Vehicles;


import retrofit2.Call;
import retrofit2.http.GET;

import retrofit2.http.Headers;
import retrofit2.http.Query;


/**
 * Data Access Object interface for getting Vehicles
 * from the National Highway and traffic administration (NHTSA) Web-API.
 *
 * Please refer to (https://vpic.nhtsa.dot.gov/api/) for further info
 */
public interface CarPropertyClient {
    @Xml
    @Headers("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
    @GET("vehicles")
    Call<Vehicles> getProperties(@Query("make") String brand, @Query("model") String model);

    @Xml
    @Headers("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
    @GET("vehicles")
    Call<String> getPropertiesAsString(@Query("make") String brand, @Query("model") String model);
}
