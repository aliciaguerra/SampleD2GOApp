package com.digital2go.sdk.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;

import com.digital2go.sdk.api.D2GORequests;
import com.digital2go.sdk.managers.LocationManager;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;

/**
 * @author Digital2Go
 * This service handles the updates of geofences in area
 */
public class GeofenceInAreaService extends Service {

    /** TAG for debug purposes */
    private final String TAG = getClass().getSimpleName();
    /** Creates a new Handler to post the task */
    private Handler handler = new Handler();
    private Runnable task = new Runnable() {
        @Override
        public void run() {
            try {
                refreshGeofences();
                handler.postDelayed(this, 300000);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            refreshGeofences();
            update();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(task);
    }

    /**
     * Updates geofences in area each 5 mins
     */
    private void update() {
        handler.postDelayed(task, 10000);
    }

    /**
     * Creates and execute the request to get geofences in area
     */
    private void refreshGeofences() throws JSONException {
        Location location = LocationManager.getInstance().getLastLocation();
        if (location != null) D2GORequests.getInstance().geofenceInRange(new LatLng(location.getLatitude(), location.getLongitude()), 500, getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
