package com.mobindustry.photoweatherapp.ui.camera_view.presenter;

import android.content.Context;
import android.widget.ImageView;

public interface WeatherPresenter {

    void findWeatherByLocation(Context context);

    void loadImage(ImageView imageView, String url);
}
