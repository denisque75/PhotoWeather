package com.mobindustry.photoweatherapp.core.retrofit;

import com.mobindustry.photoweatherapp.core.entity.Forecast;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    String FIND_CITY = "data/2.5/forecast";
    String UNITS = "metric";

    @GET(FIND_CITY)
    Call<Forecast> getForecasts(@Query("lat") double lat, @Query("lon") double lon, @Query("units") String units, @Query("APPID") String appId);
}
