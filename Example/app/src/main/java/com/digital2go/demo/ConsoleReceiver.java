package com.digital2go.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Ulises on 28/11/2017.
 */

public class ConsoleReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getBooleanExtra("_location", false)){
            double lat = intent.getDoubleExtra("_lat", 0.0);
            double lng = intent.getDoubleExtra("_lng", 0.0);

            MainActivityNew.setLocation(String.valueOf(lat), String.valueOf(lng));
        }

        String _console = intent.getStringExtra("_console");
        if (_console != null){
            Date currentDate = new Date();
            SimpleDateFormat df = new SimpleDateFormat("hh:mm");
            String time = df.format(currentDate);


            MainActivityNew.addInteraction("["+time+"] "+_console);
        }
    }
}

