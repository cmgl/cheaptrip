package com.example.cheaptrip.dao.rest;

import com.example.cheaptrip.handlers.converter.annotations.Json;
import com.example.cheaptrip.handlers.converter.annotations.Raw;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;

public interface ODSStationClient {

    @Raw
    @Headers({
            "Accept: application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8",
            "Content-Type: application/json"
    })
    @GET("test.json")
    Call<String> getStationData();
}
