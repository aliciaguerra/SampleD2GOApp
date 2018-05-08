package com.digital2go.sdk.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;

import com.digital2go.sdk.api.D2GORequests;
import com.digital2go.sdk.managers.LocationManager;
import com.digital2go.sdk.utils.SDKPreferences;
import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.configuration.scan.ScanMode;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerContract;
import com.kontakt.sdk.android.ble.manager.listeners.EddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.Proximity;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Digital2Go
 * Created by Ulises Rosas on 8/08/2016.
 */
public class BeaconService extends Service implements IBeaconListener, EddystoneListener{

    /** TAG for debug purposes */
    private final String TAG = getClass().getSimpleName();
    /** Proximity Manager from KontaktSDK */
    private static ProximityManagerContract proximityManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        KontaktSDK.initialize(getApplicationContext());
        config(getApplicationContext());
        return START_STICKY;
    }

    /**
     * Configurator for ProximityManager, restores the data from the Preferences sets the scan
     * configuration and starts the scan
     *
     * @param context Application Context
     */
    private void config(Context context) {
        proximityManager = new ProximityManager(context);
        proximityManager.setIBeaconListener(this);
        if (isForeground(context)) foregroundConfig();
        else backgroundConfig();

        startScan();
    }

    /**
     * Common Foreground Configuration
     */
    private void foregroundConfig() {
        proximityManager.configuration()
                .scanMode(ScanMode.LOW_LATENCY)
                .scanPeriod(ScanPeriod.create(TimeUnit.SECONDS.toMillis(3), TimeUnit.SECONDS.toMillis(3)))
                .activityCheckConfiguration(ActivityCheckConfiguration.MINIMAL)
                .monitoringEnabled(true);
    }

    /**
     * Common Background Configuration
     */
    private void backgroundConfig() {
        proximityManager.configuration()
                .scanMode(ScanMode.LOW_POWER)
                .scanPeriod(ScanPeriod.create(TimeUnit.SECONDS.toMillis(3), TimeUnit.SECONDS.toMillis(3)))
                .activityCheckConfiguration(ActivityCheckConfiguration.MINIMAL)
                .monitoringEnabled(true);
    }

    /**
     * Connects the ProximityManager, then start the scan for beacons
     */
    private void startScan() {
        proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                proximityManager.startScanning();
            }
        });
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (proximityManager != null) {
            proximityManager.stopScanning();
            proximityManager.disconnect();
            proximityManager = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onIBeaconDiscovered(IBeaconDevice iBeacon, IBeaconRegion region) {
        try{
            getApplicationContext().sendBroadcast(new Intent("com.d2go.interaction."+SDKPreferences.getInstance().getAppName(getApplicationContext()).toLowerCase()).putExtra("_console", "Beacon M:"+iBeacon.getMajor() + ", m:" + iBeacon.getMinor() + " found at: " + iBeacon.getProximity().toString()));
            beaconRequest(iBeacon);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onIBeaconsUpdated(List<IBeaconDevice> iBeacons, IBeaconRegion region) {
        try {
            for (IBeaconDevice device : iBeacons) {
                getApplicationContext().sendBroadcast(new Intent("com.d2go.interaction."+SDKPreferences.getInstance().getAppName(getApplicationContext()).toLowerCase()).putExtra("_console", "Beacon Update M:"+device.getMajor() + ", m:" + device.getMinor() + " now at: " + device.getProximity().toString()));
                beaconRequest(device);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onIBeaconLost(IBeaconDevice iBeacon, IBeaconRegion region) {

    }

    private void beaconRequest(IBeaconDevice iBeaconDevice) throws JSONException, UnsupportedEncodingException {
        final Context context = getApplicationContext();
        Location location = LocationManager.getInstance().getLastLocation();

        String proximity = null;
        if (iBeaconDevice.getProximity() == Proximity.IMMEDIATE) proximity = "touch";
        else proximity = iBeaconDevice.getProximity().toString().toLowerCase();

        if (location != null){
            JSONObject impression = new JSONObject();

            JSONObject deviceInfo = SDKPreferences.getInstance().getDeviceInfo(context); //to complete device info
            deviceInfo.put("altitude", location.getAltitude());
            deviceInfo.put("horizontal_accuracy", location.getAccuracy());
            deviceInfo.put("vertical_accuracy", location.getAccuracy());

            impression.put("uuid", iBeaconDevice.getProximityUUID().toString());
            impression.put("device_id", SDKPreferences.getInstance().getAndroidID(context).replace("-", ""));
            impression.put("os", "android");
            impression.put("bluetooth_enabled", 1);
            impression.put("app_name", SDKPreferences.getInstance().getAppName(context));
            impression.put("type", "beacon");
            impression.put("proximity", proximity);
            impression.put("major", iBeaconDevice.getMajor());
            impression.put("minor_dec", iBeaconDevice.getMinor());
            impression.put("lat", location.getLatitude());
            impression.put("lng", location.getLongitude());
            impression.put("timestamp", getCurrentTime());
            impression.put("demographics", SDKPreferences.getInstance().getDemographics(context));
            impression.put("deviceinfo", deviceInfo);

            getApplicationContext().sendBroadcast(new Intent("com.d2go.interaction."+SDKPreferences.getInstance().getAppName(context).toLowerCase()).putExtra("_console", "Sending Beacon Impression for: "+iBeaconDevice.getMinor()));
            getApplicationContext().sendBroadcast(new Intent("com.d2go.interaction."+SDKPreferences.getInstance().getAppName(context).toLowerCase()).putExtra("_console", "Impression Body for: "+iBeaconDevice.getMinor() + "\n" + impression.toString()));
            D2GORequests.getInstance().beaconImpression(impression, context);
        }

    }

    private String getCurrentTime(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDate = null;
        Calendar calendar = Calendar.getInstance();

        currentDate = format.format(calendar.getTime());

        return currentDate;
    }

    /**
     * To check if the app is on foreground
     * @param context application context
     * @return is the app on foreground
     */
    private static boolean isForeground(Context context) {
        // Gets a list of running processes.
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();

        // On some versions of android the first item in the list is what runs in the foreground,
        // but this is not true on all versions.  Check the process importance to see if the app
        // is in the foreground.
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : tasks) {
            if (ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND == appProcess.importance
                    && packageName.equals(appProcess.processName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onEddystoneDiscovered(IEddystoneDevice eddystone, IEddystoneNamespace namespace) {

    }

    @Override
    public void onEddystonesUpdated(List<IEddystoneDevice> eddystones, IEddystoneNamespace namespace) {

    }

    @Override
    public void onEddystoneLost(IEddystoneDevice eddystone, IEddystoneNamespace namespace) {

    }
}