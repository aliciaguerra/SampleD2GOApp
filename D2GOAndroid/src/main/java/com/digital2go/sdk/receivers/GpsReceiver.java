package com.digital2go.sdk.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import com.digital2go.sdk.utils.SDKPreferences;

/**
 * @author Digital2Go
 * Created by Ulises Rosas on 17/08/2016.
 */
public class GpsReceiver extends BroadcastReceiver {
    /** SO LocationManager */
    private LocationManager locationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        try{
            locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
            SDKPreferences preferences = SDKPreferences.getInstance();
            switch (intent.getAction()){
                case LocationManager.PROVIDERS_CHANGED_ACTION:
                    com.digital2go.sdk.managers.LocationManager manager=  com.digital2go.sdk.managers.LocationManager.getInstance();
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                        if(preferences.getLocationStatus(context.getApplicationContext())) manager.startService(context.getApplicationContext());
                        else manager.stopService(context.getApplicationContext());
                    else manager.stopService(context.getApplicationContext());
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
