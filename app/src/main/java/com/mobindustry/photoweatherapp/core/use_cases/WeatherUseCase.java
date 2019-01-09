package com.mobindustry.photoweatherapp.core.use_cases;

import android.content.Context;
import android.widget.ImageView;

import com.mobindustry.photoweatherapp.core.callbacks.NetworkCallback;
import com.mobindustry.photoweatherapp.core.entity.Forecast;

public interface WeatherUseCase {

    void findWeatherByCurrentPosition(Context context, NetworkCallback<Forecast> callback);

    void loadImage(ImageView imageView, String url);
}
