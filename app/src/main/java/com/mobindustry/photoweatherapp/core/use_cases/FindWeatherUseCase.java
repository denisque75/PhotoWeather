package com.mobindustry.photoweatherapp.core.use_cases;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.mobindustry.photoweatherapp.BuildConfig;
import com.mobindustry.photoweatherapp.core.callbacks.NetworkCallback;
import com.mobindustry.photoweatherapp.core.dto.Dimens;
import com.mobindustry.photoweatherapp.core.entity.Forecast;
import com.mobindustry.photoweatherapp.core.repositories.location.LocationRepository;
import com.mobindustry.photoweatherapp.core.repositories.photo.PhotoRepository;
import com.mobindustry.photoweatherapp.core.repositories.weather.ForecastRepository;

public class FindWeatherUseCase implements WeatherUseCase {
    private static final String TAG = "FindWeatherUseCase";

    private final LocationRepository locationRepository;
    private final ForecastRepository forecastRepository;
    private final PhotoRepository photoRepository;

    public FindWeatherUseCase(LocationRepository locationRepository, ForecastRepository forecastRepository, PhotoRepository photoRepository) {
        this.locationRepository = locationRepository;
        this.forecastRepository = forecastRepository;
        this.photoRepository = photoRepository;
    }

    @Override
    public void findWeatherByCurrentPosition(Context context, final NetworkCallback<Forecast> callback) {
        locationRepository.findLocation(context, new NetworkCallback<Dimens>() {
            @Override
            public void onSuccess(Dimens dimens) {
                Log.d(TAG, "onSuccess: lat = " + dimens.getLat() + " lon = " + dimens.getLon());
                forecastRepository.loadForecast(dimens, callback);
            }

            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure(throwable);
            }
        });
    }

    @Override
    public void loadImage(ImageView imageView, String url) {
        photoRepository.loadImage(imageView, BuildConfig.PIC_URL + url + BuildConfig.PIC_EXT);
    }
}
