package com.digital2go.sdk;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.digital2go.sdk.api.D2GORequests;
import com.digital2go.sdk.exceptions.LoginException;
import com.digital2go.sdk.exceptions.SDKException;
import com.digital2go.sdk.managers.BeaconManager;
import com.digital2go.sdk.managers.LocationManager;
import com.digital2go.sdk.services.GeofenceInAreaService;
import com.digital2go.sdk.services.RefreshService;
import com.digital2go.sdk.utils.SDKPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import static com.digital2go.sdk.utils.SDKPreferences.getInstance;

/**
 * @author Digital2Go
 * Created by Ulises Rosas on 8/2/16.
 */

public final class D2GOSDK {
    private static Context appContext;

    /** start beacon service */
    public static void startBeaconService() throws SDKException, LoginException, IllegalStateException {
        SDKPreferences preferences = SDKPreferences.getInstance();
        if (appContext != null) {
            if (preferences.getApiConnection(appContext)) {
                if (preferences.getDemographics(appContext) != null){
                    preferences.setBeaconStatus(true, appContext);
                    BeaconManager.getInstance().startService(appContext);
                }else throw new IllegalStateException("Please update Demographics");
            } else throw new LoginException("No Connection to Digital2Go API you need to Login, try init(...)");
        }else throw new SDKException("SDK not initialized, try init(...)");
    }

    /** stop beacon Service */
    public static void stopBeaconService(){
        if (appContext != null){
            SDKPreferences.getInstance().setBeaconStatus(false, appContext);
            BeaconManager.getInstance().stopService(appContext);
        }
    }

    /** start Location Service */
    public static void startLocationService() throws SDKException, LoginException, IllegalStateException {
        SDKPreferences preferences = SDKPreferences.getInstance();
        if (appContext != null) {
            if (preferences.getApiConnection(appContext)) {
                if (preferences.getDemographics(appContext) != null) {
                    preferences.setLocationStatus(true, appContext);
                    LocationManager.getInstance().startService(appContext);
                }else throw new IllegalStateException("Please update Demographics");
            } else throw new LoginException("No Connection to Digital2Go API you need to Login, did you tried init(...)");
        } else throw new SDKException("SDK not initialized, try init(...)");
    }

    /** stop location service */
    public static void stopLocationService() {
        if (appContext != null){
            SDKPreferences.getInstance().setLocationStatus(false, appContext);
            LocationManager.getInstance().stopService(appContext);
        }
    }

    /**
     * Gets the Beacon Service Status stored in sharedPreferences
     * @return true if enabled, false otherwise
     */
    public static boolean beaconServiceStatus(){
        if (appContext != null) return SDKPreferences.getInstance().getBeaconsStatus(appContext);
        else return false;
    }

    /**
     * Gets the Location Service Status stored in sharedPreferences
     * @return true if enabled, false otherwise
     */
    public static boolean locationServiceStatus(){
        if (appContext != null) return SDKPreferences.getInstance().getLocationStatus(appContext);
        else return false;
    }

    /**
     * This method initializes the sdk functions
     * @param context Application Context
     */
    public static synchronized void init(final Context context, String token){
        if(context != null){
            appContext = context.getApplicationContext();
            SDKPreferences preferences = getInstance();
            ApplicationInfo appInfo;
            try {
                appInfo = appContext.getPackageManager().getApplicationInfo(context.getPackageName(), 128);
            }catch (PackageManager.NameNotFoundException e){
                throw new IllegalStateException("Could not find package name");
            }

            preferences.storeFirebaseToken(token, context);
            String appName = appInfo.metaData.getString("app_name");

            if(appName != null && !appName.isEmpty()){
                String username = appInfo.metaData.getString("d2go_app_username");
                String password = appInfo.metaData.getString("d2go_app_password");

                preferences.setAppName(appName, appContext);

                    if(username != null && !username.isEmpty() && password != null && !password.isEmpty()){
                        try {
                            D2GORequests.getInstance().login(username, password);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else{
                        throw new IllegalStateException("D2GO Username or Password missing");
                    }
            }else{
                throw new IllegalStateException("app_name is missing");
            }
        }else{
            throw new IllegalStateException("D2GOSDK Needs an Application Context");
        }
    }

    /** Stops sdk execution(Stops services and disable connection) */
    public static void stop(){
        if (appContext != null){
            SDKPreferences preferences = SDKPreferences.getInstance();

            preferences.ApiConnection(false, appContext);
            stopBeaconService();
            stopLocationService();
            appContext.stopService(new Intent(appContext, GeofenceInAreaService.class));
            appContext.stopService(new Intent(appContext, RefreshService.class));
        }
    }

    /** Update demographics */
    public static void updateDemographics(JSONArray demographics, final Context context){
        SDKPreferences preferences = SDKPreferences.getInstance();
        preferences.storeDemographics(demographics.toString(), context);
    }

    /**
     * This method returns the status of the sdk
     * @return True if connected, false otherwise
     */
    public static boolean isConnected(final Context context){
        return SDKPreferences.getInstance().getApiConnection(context);
    }

    /**
     * Get token stored in memory
     * @return JSONObject with token
     */
    public static JSONObject getToken(final Context context){
        JSONObject token = null;
            token = SDKPreferences.getInstance().getToken(context);
        return token;
    }

    /**
     * Get demographic info
     * @return JSONObject with info
     */
    public static JSONArray getDemographics(final Context context){
        JSONArray demographics = null;
            demographics = SDKPreferences.getInstance().getDemographics(context);
        return demographics;
    }

    /**
     * Get home tiles
     * @return JSONObject with info
     */
    public static JSONArray getTiles(){
        JSONArray tiles = null;
            tiles = SDKPreferences.getInstance().get_tiles(appContext);
        return tiles;
    }

    /**
     * Changes device id
     * @param context Application context
     * @throws SDKException
     * @throws JSONException
     */
    public static void changeDeviceId(final Context context) throws SDKException, JSONException {
        SDKPreferences preferences = SDKPreferences.getInstance();
        if (preferences.getApiConnection(context)){
            String id = preferences.getAndroidID(context);

            if (id != null) D2GORequests.getInstance().change_deviceId(id, context);
        }else throw new SDKException("SDK not connected, try init(...)");
    }

    /**
     * Registers interaction to the api
     * @param campaign Campaign interaction to register
     * @param context
     * @throws SDKException
     * @throws UnsupportedEncodingException
     */
    public static void registerInteraction(JSONObject campaign, final Context context) throws SDKException, UnsupportedEncodingException {
        try {
            SDKPreferences preferences = SDKPreferences.getInstance();
            if (preferences.getApiConnection(context)) {
                D2GORequests.getInstance().register_interaction(campaign, context);
            } else throw new SDKException("SDK not connected, try init(...)");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get device info stored
     * @return JSONObject with info
     * @throws JSONException
     */
    public static JSONObject getDeviceInfo(final Context context) throws JSONException {
        JSONObject device = null;

            device = SDKPreferences.getInstance().getDeviceInfo(appContext);

        return device;
    }

    /** to retrieve static appContext */
    public static Context getAppContext() {
        return appContext;
    }

}
