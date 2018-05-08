package com.digital2go.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import com.digital2go.demo.fragments.FragmentSearch;

/**
 * Created by Ulises on 12/02/2018.
 */

public class GPSReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final LocationManager manager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            // Call your Alert message
            FragmentSearch.setStatusColor(R.color.colorError, R.string.gps_alert_title, R.string.gps_alert_message, true, R.mipmap.ic_gps);
            context.sendBroadcast(new Intent("com.d2go.interaction."+String.valueOf(R.string.app_name).toLowerCase()).putExtra("_console", "GPS Disabled"));
        }else {
            FragmentSearch.setStatusColor(R.color.pure_black, 0, 0, false, 0);
            context.sendBroadcast(new Intent("com.d2go.interaction"+String.valueOf(R.string.app_name).toLowerCase()).putExtra("_console", "GPS Enabled"));
        }
    }
}
