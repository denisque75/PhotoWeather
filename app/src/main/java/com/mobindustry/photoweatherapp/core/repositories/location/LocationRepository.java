package com.mobindustry.photoweatherapp.core.repositories.location;

import android.content.Context;

import com.mobindustry.photoweatherapp.core.callbacks.NetworkCallback;
import com.mobindustry.photoweatherapp.core.dto.Dimens;

public interface LocationRepository {

    void findLocation(Context context, NetworkCallback<Dimens> callback);
}
