package com.mobindustry.photoweatherapp.ui.camera_view;

import com.mobindustry.photoweatherapp.core.entity.Forecast;
import com.mobindustry.photoweatherapp.ui.MessageView;
import com.mobindustry.photoweatherapp.ui.ProgressView;

public interface CameraView extends MessageView, ProgressView {

    void showForecastOnCameraView(Forecast forecast);
}
