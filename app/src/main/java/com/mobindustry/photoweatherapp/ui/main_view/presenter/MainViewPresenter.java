package com.mobindustry.photoweatherapp.ui.main_view.presenter;

import android.content.Context;
import android.widget.ImageView;

public interface MainViewPresenter {

    void findWeatherByPosition(Context context);

    void showImage(ImageView imageView, String url);
}
