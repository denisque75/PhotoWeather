package com.mobindustry.photoweatherapp.ui.camera_view.presenter;

import android.content.Context;
import android.widget.ImageView;

import com.mobindustry.photoweatherapp.core.callbacks.NetworkCallback;
import com.mobindustry.photoweatherapp.core.entity.Forecast;
import com.mobindustry.photoweatherapp.core.use_cases.WeatherUseCase;
import com.mobindustry.photoweatherapp.ui.camera_view.CameraView;

public class CameraWeatherPresenter implements WeatherPresenter {
    private final WeatherUseCase weatherUseCase;
    private final CameraView cameraView;

    public CameraWeatherPresenter(WeatherUseCase weatherUseCase, CameraView cameraView) {
        this.weatherUseCase = weatherUseCase;
        this.cameraView = cameraView;
    }

    @Override
    public void findWeatherByLocation(Context context) {
        cameraView.showProgress(true);
        weatherUseCase.findWeatherByCurrentPosition(context, new NetworkCallback<Forecast>() {
            @Override
            public void onSuccess(Forecast forecast) {
                cameraView.showForecastOnCameraView(forecast);
                cameraView.showProgress(false);
            }

            @Override
            public void onFailure(Throwable throwable) {
                cameraView.showMessage("Something went wrong!");
                cameraView.showProgress(false);
            }
        });
    }

    @Override
    public void loadImage(ImageView imageView, String url) {
        weatherUseCase.loadImage(imageView, url);
    }
}
