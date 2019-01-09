package com.mobindustry.photoweatherapp.core.callbacks;

public interface NetworkCallback<T> {

    void onSuccess(T t);

    void onFailure(Throwable throwable);
}
