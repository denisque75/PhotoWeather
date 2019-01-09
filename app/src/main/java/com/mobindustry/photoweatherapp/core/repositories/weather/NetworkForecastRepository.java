package com.mobindustry.photoweatherapp.core.repositories.weather;

import android.support.annotation.NonNull;
import android.util.Log;

import com.mobindustry.photoweatherapp.BuildConfig;
import com.mobindustry.photoweatherapp.core.callbacks.NetworkCallback;
import com.mobindustry.photoweatherapp.core.dto.Dimens;
import com.mobindustry.photoweatherapp.core.entity.Forecast;
import com.mobindustry.photoweatherapp.core.retrofit.WeatherApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NetworkForecastRepository implements ForecastRepository {
    private static final String TAG = "NetworkForecastReposito";
    private final WeatherApi weatherApi;

    public NetworkForecastRepository(WeatherApi weatherApi) {
        this.weatherApi = weatherApi;
    }

    @Override
    public void loadForecast(Dimens dimens, final NetworkCallback<Forecast> callback) {
        weatherApi
                .getForecasts(dimens.getLat(), dimens.getLon(), WeatherApi.UNITS, BuildConfig.API_KEY)
                .enqueue(new Callback<Forecast>() {
                    @Override
                    public void onResponse(@NonNull Call<Forecast> call, @NonNull Response<Forecast> response) {
                        Forecast forecast = response.body();
                        Log.d(TAG, "onResponse: forecast:" + forecast);
                        callback.onSuccess(forecast);
                    }

                    @Override
                    public void onFailure(@NonNull Call<Forecast> call, @NonNull Throwable t) {
                        Log.e(TAG, "onFailure: ", t);
                        callback.onFailure(t);
                    }
                });
    }
}
