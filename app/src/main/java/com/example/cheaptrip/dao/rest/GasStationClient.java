package com.example.cheaptrip.dao.rest;

import com.example.cheaptrip.handlers.converter.annotations.Csv;
import com.example.cheaptrip.handlers.converter.annotations.Raw;
import com.example.cheaptrip.models.tankerkoenig.GasStationResponse;
import com.example.cheaptrip.models.tankerkoenig.Station;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;


/**
 * TODO: Document
 */
public interface GasStationClient {

    String API_KEY = "ad5b5cac-db85-4516-832d-1bc90df23946";
    /**
     * Enum to determine the Fuel Type for the REST-API Call
     */
    enum FuelType
    {
        E5("e5"),
        E10("e10"),
        DIESEL("diesel"),
        ALL("all");

        private String fuelType;

        FuelType(String fuelType) {
            this.fuelType = fuelType;
        }

        public String getFuelType() {
            return fuelType;
        }
    }
    /**
     * Enum to determine how to sort the results of the REST-API Call
     */
    enum Sort
    {
        PRICE("price"),
        DISTANCE("dist");

        private String sort;

        Sort(String sort) {
            this.sort = sort;
        }

        public String getSort() {
            return sort;
        }
    }




    /**
     * TODO: Document
     * @param lat
     * @param lon
     * @param radius
     * @param type
     * @param sort
     * @return
     */
    @GET("json/list.php?apikey="+API_KEY)
    Call<GasStationResponse> GetAllForRadius(@Query("lat") Double lat,
                                             @Query("lng") Double lon,
                                             @Query("rad") Double radius,
                                             @Query("type") FuelType type,
                                             @Query("sort") Sort sort,
                                             @Query("sort") String apiKey
    );

    @Raw
    @GET("json/list.php?apikey="+API_KEY)
    Call<String> GetAllForRadiusJSON(@Query("lat") Double lat,
                                     @Query("lng") Double lon,
                                     @Query("rad") Double radius,
                                     @Query("type") String type,
                                     @Query("sort") String sort,
                                     @Query("sort") String apiKey
    );


    /**
     * Gets all relevant GasStation with a specific radius(provided by argument radius).
     * The area to search is determined by coordinate having Longitude long and Latitude lat
     * For more info refer to https://tankerkoenig.de/
     *
     * @param lat   Latitude to start radial search
     * @param lon   Longitude to start radial search
     * @param radius    Radius to start radial search
     * @param type      type of fuel
     * @param sort      by distance or duration
     * @return          A WebserviceResonse containing all Gasstations within a radius
     */
    //@GET("json/list.php?apikey="+API_KEY)
    @GET("json/list.php")
    Call<GasStationResponse> getStationsInRadius(   @Query("lat") Double lat,
                                                    @Query("lng") Double lon,
                                                    @Query("rad") Double radius,
                                                    @Query("type") String type,
                                                    @Query("sort") String sort,
                                                    @Query("apikey") String apiKey
    );

    @Csv
    @Streaming
    @GET("items")
    Call<List<Station>> getHistory(@Query("path") String path);


    @Raw
    @Streaming
    @GET("items")
    Call<String> getHistoryAsString(@Query("path") String path);
}
