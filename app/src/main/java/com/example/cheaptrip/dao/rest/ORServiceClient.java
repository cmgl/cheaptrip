package com.example.cheaptrip.dao.rest;

import com.example.cheaptrip.handlers.converter.annotations.Json;
import com.example.cheaptrip.handlers.converter.annotations.Raw;
import com.example.cheaptrip.models.orservice.GeoMatrixResponse;
import com.example.cheaptrip.models.orservice.ORServiceResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ORServiceClient {
    String API_KEY = "5b3ce3597851110001cf6248acbf784528c542769640c46c08f9dd07";
    //String API_KEY = "5b3ce3597851110001cf624854d7d252265340ecba56c53ca4377994";

    @Raw
    @Headers({
            "Authorization: " + API_KEY ,
            "Accept: application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8",
            "Content-Type: application/json"
    })
    @POST("directions/driving-car/geojson")
    Call<String> getGeoJson(@Body String body);



    @Json
    @Headers({
            "Authorization: " + API_KEY ,
            "Accept: application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8",
            "Content-Type: application/json"
    })
    @POST("directions/driving-car/geojson")
    Call<ORServiceResponse> getDirections(@Body String body);


    @Json
    @FormUrlEncoded
    @Headers({
            "Authorization: " + API_KEY ,
            "Accept: application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8",
            "Content-Type: application/json"
    })
    @POST("directions/driving-car/geojson")
    Call<ORServiceResponse> getDirections2(@Field("coordinates")List<List<Double>> coordinates,@Field("unit") String unit);

    @Json
    @Headers({
            "Authorization: " + API_KEY ,
            "Accept: application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8",
            "Content-Type: application/json; charset=utf-8"
    })
    @POST("matrix/driving-car")
    Call<GeoMatrixResponse> getMatrix(@Body String body);

}
