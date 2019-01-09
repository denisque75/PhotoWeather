package com.mobindustry.photoweatherapp.core.entity;

import com.google.gson.annotations.SerializedName;

public class Weather {

    private long weatherId;

    @SerializedName("main.humidity")
    private int humidity;

    @SerializedName("main.temp")
    private float temp;

    private long time;
    private String mainWeatherDesc;
    private String icon;

    public Weather() {
    }

    public Weather(int weatherId, int humidity, float temp, long time, String mainWeatherDesc, String icon) {
        this.weatherId = weatherId;
        this.humidity = humidity;
        this.temp = temp;
        this.time = time;
        this.mainWeatherDesc = mainWeatherDesc;
        this.icon = icon;
    }

    public long getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(long weatherId) {
        this.weatherId = weatherId;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public float getTemp() {
        return temp;
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getMainWeatherDesc() {
        return mainWeatherDesc;
    }

    public void setMainWeatherDesc(String mainWeatherDesc) {
        this.mainWeatherDesc = mainWeatherDesc;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return "Weather{" +
                ", weatherId=" + weatherId +
                ", humidity=" + humidity +
                ", temp=" + temp +
                ", time=" + time +
                ", mainWeatherDesc='" + mainWeatherDesc + '\'' +
                ", icon='" + icon + '\'' +
                '}';
    }
}
