package com.mobindustry.photoweatherapp.core.dto;

public class Dimens {
    private double lon;
    private double lat;

    public Dimens() {
    }

    public Dimens(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    @Override
    public String toString() {
        return "Dimens{" +
                "lon=" + lon +
                ", lat=" + lat +
                '}';
    }
}
