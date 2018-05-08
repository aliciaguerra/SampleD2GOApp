package com.digital2go.sdk.managers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import com.digital2go.sdk.D2GOSDK;
import com.digital2go.sdk.exceptions.SDKException;
import com.digital2go.sdk.services.LocationService;

/**
 * @author Digital2Go
 * Created by Ulises Rosas on 24/08/2016.
 */
public class LocationManager {
    private static LocationManager instance = null;

    /**
     * LastLocation Registered
     */
    private Location lastLocation;

    /**
     * Singleton
     * @return LocationManager instance
     */
    public static synchronized LocationManager getInstance(){
        if(instance == null) instance = new LocationManager();
        return instance;
    }

    /** Starts the Location Service */
    public void startService(final Context context) throws SDKException {
        if (context != null) {
            boolean location = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION);
            boolean locationGPS = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
            if (location && locationGPS) context.startService(new Intent(context, LocationService.class));
        }
    }

    /** stop location service */
    public void stopService(final Context context) { if (context != null) context.stopService(new Intent(context, LocationService.class)); }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location location){
        this.lastLocation = location;
    }
}
