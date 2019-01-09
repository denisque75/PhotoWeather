package com.mobindustry.photoweatherapp.core.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mobindustry.photoweatherapp.BuildConfig;
import com.mobindustry.photoweatherapp.core.entity.Forecast;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitCreator {

    private final Retrofit retrofit;

    public RetrofitCreator (Retrofit retrofit) {
        this.retrofit = retrofit;
    }

    public <T> T create(Class<T> t) {
        return retrofit.create(t);
    }

    public static Retrofit createRetrofit() {
        Gson gson = new GsonBuilder().registerTypeAdapter(Forecast.class, new CustomDeserializer()).create();
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(new OkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
}
