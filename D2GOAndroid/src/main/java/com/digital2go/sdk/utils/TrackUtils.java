package com.digital2go.sdk.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.digital2go.sdk.receivers.TrackReceiver;

import java.util.Calendar;

/**
 * @author Digital2Go
 * Created by Ulises on 24/11/2017.
 */

public class TrackUtils {
    private static AlarmManager alarmMgr;
    private static PendingIntent alarmIntent;

    public static void enableTrack(final Context context){
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TrackReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        setTrackDay();
        setTrackNight();
    }

    private static void setTrackDay(){
        // Set the alarm to start at 23:59 PM
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    private static void setTrackNight(){
        // Set the alarm to start at 11:00 AM
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        calendar.set(Calendar.MINUTE, 0);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    public static void disableTrack(final Context context){
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TrackReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarmMgr.cancel(alarmIntent);
    }
}
