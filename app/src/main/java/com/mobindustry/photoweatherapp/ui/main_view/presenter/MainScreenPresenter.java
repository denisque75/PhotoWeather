package com.mobindustry.photoweatherapp.ui.main_view.presenter;

import android.content.Context;
import android.widget.ImageView;

import com.mobindustry.photoweatherapp.core.callbacks.NetworkCallback;
import com.mobindustry.photoweatherapp.core.entity.Forecast;
import com.mobindustry.photoweatherapp.core.use_cases.WeatherUseCase;
import com.mobindustry.photoweatherapp.ui.main_view.MainView;

public class MainScreenPresenter implements MainViewPresenter {
    private final WeatherUseCase weatherUseCase;
    private final MainView mainView;

    public MainScreenPresenter(WeatherUseCase weatherUseCase, MainView mainView) {
        this.weatherUseCase = weatherUseCase;
        this.mainView = mainView;
    }

    @Override
    public void findWeatherByPosition(Context context) {
        mainView.showProgress(true);
        weatherUseCase.findWeatherByCurrentPosition(context, new NetworkCallback<Forecast>() {
            @Override
            public void onSuccess(Forecast forecast) {
                mainView.showResult(forecast);
                mainView.showProgress(false);
            }

            @Override
            public void onFailure(Throwable throwable) {
                mainView.showMessage("Something went wrong");
                mainView.showProgress(false);
            }
        });
    }

    @Override
    public void showImage(ImageView imageView, String url) {
        weatherUseCase.loadImage(imageView, url);
    }
}
