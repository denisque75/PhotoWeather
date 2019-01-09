package com.mobindustry.photoweatherapp.core.repositories.weather;

import com.mobindustry.photoweatherapp.core.callbacks.NetworkCallback;
import com.mobindustry.photoweatherapp.core.dto.Dimens;
import com.mobindustry.photoweatherapp.core.entity.Forecast;

public interface ForecastRepository {

    void loadForecast(Dimens dimens, NetworkCallback<Forecast> callback);
}
