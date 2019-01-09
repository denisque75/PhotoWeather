package com.mobindustry.photoweatherapp.ui.main_view;

import com.mobindustry.photoweatherapp.core.entity.Forecast;
import com.mobindustry.photoweatherapp.ui.MessageView;
import com.mobindustry.photoweatherapp.ui.ProgressView;

public interface MainView extends MessageView, ProgressView {

    void showResult(Forecast forecast);

}
