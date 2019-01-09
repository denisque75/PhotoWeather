package com.mobindustry.photoweatherapp.core.repositories.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.mobindustry.photoweatherapp.core.callbacks.NetworkCallback;
import com.mobindustry.photoweatherapp.core.dto.Dimens;
import com.mobindustry.photoweatherapp.core.entity.Forecast;

import java.util.Timer;
import java.util.TimerTask;

public class CurrentLocationRepository implements LocationRepository {
    private static final String TAG = "CurrentLocationReposito";

    public static final int REQUEST_CHECK_SETTINGS = 7766;
    public static final int GET_LOCATION_ATTEMPT_DURATION = 15000;

    private Timer timer1;

    private LocationManager lm;

    private NetworkCallback<Dimens> callback;

    boolean gps_enabled = false;
    boolean network_enabled = false;

    @SuppressLint("MissingPermission")
    @Override
    public void findLocation(Context context, final NetworkCallback<Dimens> callback) {

        this.callback = callback;

        // create location request for check Location Settings
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // create Location Settings Request builder
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);

        // check Location Settings
        SettingsClient client = LocationServices.getSettingsClient(context);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                getLocation(context);
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onFailure(e);
            }
        });

    }

    @SuppressWarnings("MissingPermission")
    private void getLocation(Context context) {
        if (lm == null)
            lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //exceptions will be thrown if provider is not permitted.
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //don't start listeners if no provider is enabled
        if (!gps_enabled && !network_enabled) {
            callback.onSuccess(null);

            return;
        }

        if (gps_enabled)
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
        if (network_enabled)
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
        timer1 = new Timer();
        timer1.schedule(new GetLastLocation(), GET_LOCATION_ATTEMPT_DURATION);
    }

    @SuppressWarnings("MissingPermission")
    final private LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer1.cancel();
            callback.onSuccess(new Dimens(location.getLongitude(),location.getLatitude()));

            lm.removeUpdates(this);
            lm.removeUpdates(locationListenerNetwork);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    @SuppressWarnings("MissingPermission")
    final private LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer1.cancel();
            callback.onSuccess(new Dimens(location.getLongitude(),location.getLatitude()));

            lm.removeUpdates(this);
            lm.removeUpdates(locationListenerGps);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    @SuppressWarnings("MissingPermission")
    class GetLastLocation extends TimerTask {
        @Override
        public void run() {
            lm.removeUpdates(locationListenerGps);
            lm.removeUpdates(locationListenerNetwork);

            Location location = gotLastLocation();
            callback.onSuccess(new Dimens(location.getLongitude(), location.getLatitude()));
        }
    }

    @SuppressWarnings("MissingPermission")
    @Nullable
    /**
     * getLastKnownLocation doesn't return the current location.
     * It returns the last location it figured out via that provider.
     * That position could be an hour old- it will still return it, if it has it.
     * If you want to get an accurate location you need to turn on the location provider
     * by calling requestUpdates or requestSingleUpdate, then wait for the onLocationChanged
     * function to be called at least once. Anything else may be a stale location.
     *
     * USE THIS METHOD ONLY WHEN requestUpdates or requestSingleUpdate DON'T receive
     * any location for a GET_LOCATION_ATTEMPT_DURATION time value
     */
    private Location gotLastLocation() {
        Location net_loc = null, gps_loc = null;
        if (gps_enabled)
            gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (network_enabled)
            net_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (gps_loc != null) {
            return gps_loc;
        }
        if (net_loc != null) {
            return net_loc;
        }
        return null;
    }
}
