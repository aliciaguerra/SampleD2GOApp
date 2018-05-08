package com.digital2go.sdk.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.digital2go.sdk.D2GOSDK;
import com.digital2go.sdk.domain.GeofencesInArea;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.digital2go.sdk.utils.SDKConstants.ANDROID_DEVICE_ID;
import static com.digital2go.sdk.utils.SDKConstants.APP_STATUS_PREFS_NAME;
import static com.digital2go.sdk.utils.SDKConstants.APP_TOKEN_LOGIN;
import static com.digital2go.sdk.utils.SDKConstants.D2GO_API_STATUS;
import static com.digital2go.sdk.utils.SDKConstants.DEMOGRAPHICS;
import static com.digital2go.sdk.utils.SDKConstants.FIREBASE_TOKEN;
import static com.digital2go.sdk.utils.SDKConstants.GEOFENCES_IN_AREA;
import static com.digital2go.sdk.utils.SDKConstants.GEOFENCES_LIST;
import static com.digital2go.sdk.utils.SDKConstants.HOME_TILES;
import static com.digital2go.sdk.utils.SDKConstants.SDK_BEACON_STATUS;
import static com.digital2go.sdk.utils.SDKConstants.SDK_CONFIG_FILE;
import static com.digital2go.sdk.utils.SDKConstants.SDK_LOCATION_STATUS;

/**
 * @author Sahuarolabs
 * Contains all the methods to interact with the sharedPreferences
 * Created by Ulises Rosas on 11/08/2016.
 */
public class SDKPreferences {
    private static SDKPreferences instance = new SDKPreferences();

    protected SDKPreferences() {}

    /**
     * Singleton
     * @return instance
     */
    public static synchronized SDKPreferences getInstance(){
        if (instance == null) instance = new SDKPreferences();
        return instance;
    }

    /**
     * @return beacon service status, default: 'false'
     */
    public boolean getBeaconsStatus(final Context context) {
        return context.getSharedPreferences(SDK_CONFIG_FILE, Context.MODE_PRIVATE).getBoolean(SDK_BEACON_STATUS, false);
    }

    /**
     * set beacon service status
     * @param value status
     */
    public void setBeaconStatus(Boolean value, final Context context) {
        context.getSharedPreferences(SDK_CONFIG_FILE, Context.MODE_PRIVATE).edit().putBoolean(SDK_BEACON_STATUS, value).apply();
    }

    /**
     * @return location service status, default: 'false'
     */
    public boolean getLocationStatus(final Context context) {
        return context.getSharedPreferences(SDK_CONFIG_FILE, Context.MODE_PRIVATE).getBoolean(SDK_LOCATION_STATUS, false);
    }

    /**
     * set location service status
     * @param value status
     */
    public void setLocationStatus(Boolean value, final Context context) {
        context.getSharedPreferences(SDK_CONFIG_FILE, Context.MODE_PRIVATE).edit().putBoolean(SDK_LOCATION_STATUS, value).apply();
    }

    /**
     * Stores the AndroidID to sharedPreferences
     */
    public void storeAndroidID(final Context context){
        if (context != null)
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                AdvertisingIdClient.Info idInfo = null;
                try {
                    idInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace(); }
                String advertId = null;
                try{
                    advertId = idInfo.getId();
                }catch (NullPointerException e){
                    e.printStackTrace();
                }

                context.getSharedPreferences(SDK_CONFIG_FILE, Context.MODE_PRIVATE).edit().putString(ANDROID_DEVICE_ID, advertId).apply();
            }
        });
    }

    /**
     * Gets the AndroidID stored in sharedPreferences
     * @return String with AndroidID
     */
    public String getAndroidID(final Context context){
        return context.getSharedPreferences(SDK_CONFIG_FILE, Context.MODE_PRIVATE).getString(ANDROID_DEVICE_ID, "");
    }

    /**
     * Stores Api Connection status
     * @param value Status
     */
    public void ApiConnection(boolean value, final Context context){
        context.getSharedPreferences(SDK_CONFIG_FILE, Context.MODE_PRIVATE).edit().putBoolean(D2GO_API_STATUS, value).apply();
    }

    /**
     * Gets Api connection status stored in sharedPreferences
     * @return true if connected, false otherwise
     */
    public boolean getApiConnection(final Context context){
        return context.getSharedPreferences(SDK_CONFIG_FILE, Context.MODE_PRIVATE).getBoolean(D2GO_API_STATUS, false);
    }

    /**
     * Stores token to sharedPreferences
     * @param json token response
     */
    public void storeToken(String json, final Context context){
        context.getSharedPreferences(APP_STATUS_PREFS_NAME, Context.MODE_PRIVATE).edit().putString(APP_TOKEN_LOGIN, json).apply();
    }

    /**
     * Get JWT from sharedPreferences
     */
    public JSONObject getToken(final Context context){
        String json = context.getSharedPreferences(APP_STATUS_PREFS_NAME,D2GOSDK.getAppContext().MODE_PRIVATE).getString(APP_TOKEN_LOGIN, null);
        if (json != null) { //if something's stored
            try {
                return new JSONObject(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Stores firebase token to sharedPreferences
     * @param fbtoken firebase token 
     */
    public void storeFirebaseToken(String fbtoken, final Context context){
        context.getSharedPreferences(APP_STATUS_PREFS_NAME, Context.MODE_PRIVATE).edit().putString(FIREBASE_TOKEN, fbtoken).apply();
    }

    /**
     * Gets the Firebase Token Stored
     * @return String with FirebaseToken
     */
    public String getFirebaseToken(final Context context){
        return context.getSharedPreferences(APP_STATUS_PREFS_NAME, Context.MODE_PRIVATE).getString(FIREBASE_TOKEN, "");
    }
    
    /**
     * store demographics
     * @param demographics demographics object as string
     */
    public void storeDemographics(String demographics, final Context context){
        context.getSharedPreferences(APP_STATUS_PREFS_NAME, Context.MODE_PRIVATE).edit().putString(DEMOGRAPHICS, demographics).apply();
    }


    /**
     * Gets demographics stored in sharedPreferences
     * @return JSONObject stored from string
     */
    public JSONArray getDemographics(final Context context){
        String demographicString = context.getSharedPreferences(APP_STATUS_PREFS_NAME, Context.MODE_PRIVATE).getString(DEMOGRAPHICS, null);

        if (demographicString != null) { //if something's stored
            try {
                return new JSONArray(demographicString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * store geofences
     * @param geofences geofences as string
     */
    public void storeGeofencesInArea(String geofences, final Context context){
        context.getSharedPreferences(SDK_CONFIG_FILE, Context.MODE_PRIVATE).edit()
                .putString(GEOFENCES_IN_AREA, geofences)
                .apply();

        updateGeofencesInArea(getGPSCampaigns(context), context);
    }

    /**
     * Updates list of geofences in area
     * @param geofencesInArea List of geofences in area
     */
    public void updateGeofencesInArea(List<GeofencesInArea> geofencesInArea, final Context context){
        context.getSharedPreferences(SDK_CONFIG_FILE, Context.MODE_PRIVATE).edit().putString(GEOFENCES_LIST, new Gson().toJson(geofencesInArea))
                .apply();
    }

    /**
     * list of genfences in area
     * @return list
     */
    public List<GeofencesInArea> getGeofencesList(final Context context){
        TypeToken<List<GeofencesInArea>> token = new TypeToken<List<GeofencesInArea>>() {};
        String geofences = context.getSharedPreferences(SDK_CONFIG_FILE, Context.MODE_PRIVATE).getString(GEOFENCES_LIST, null);

        if (geofences == null) return new ArrayList<>();
        else return new Gson().fromJson(geofences, token.getType());
    }

    public JSONArray getGeofenceArray(final Context context) throws JSONException {
        String geofences = context.getSharedPreferences(SDK_CONFIG_FILE, Context.MODE_PRIVATE)
                .getString(GEOFENCES_LIST, null);

        return new JSONArray(geofences);
    }

    /**
     * Returns String in Json Format with the Geofences in Area stored in sharedPreferencecs
     * @return String with Geofences
     */
    private String getGeofencesInArea(final Context context){
        return context.getSharedPreferences(SDK_CONFIG_FILE, Context.MODE_PRIVATE)
                .getString(GEOFENCES_IN_AREA, null);
    }

    private List<GeofencesInArea> getGPSCampaigns(final Context context){
        List<GeofencesInArea> campaigns = new ArrayList<>();
        GeofencesInArea gpsCampaign;
        String value = getGeofencesInArea(context);
        try {
            if (!value.isEmpty() && value != null){
                JSONArray array = new JSONObject(value).getJSONArray("data");
                if(array.length()>0)
                    for (int i=0;i<array.length();i++){
                        gpsCampaign= new GeofencesInArea();
                        gpsCampaign.setCampaign_id(array.getJSONObject(i).getString("campaign_id"));
                        gpsCampaign.setCoverage_type(array.getJSONObject(i).getString("coverage_type"));
                        String coverage = array.getJSONObject(i).getString("coverage");

                        coverage = coverage.replaceAll("'\'", "");
                        coverage = coverage.replace("[", "");
                        coverage = coverage.replace("]", "");
                        String[] coverageArray = coverage.split(",");

                        double lat = 0,lng=0;
                        String latlng = "";
                        //lista de lat long
                        List<LatLng> latLngs = new ArrayList<>();
                        for (int j = 0; j < coverageArray.length; j++) {
                            latlng = coverageArray[j].replace("\"", "");

                            try{
                                if (j%2 == 0) {
                                    //es latitud
                                    lat = Double.valueOf(latlng);
                                }else{
                                    //es longitud
                                    lng = Double.valueOf(latlng);
                                    latLngs.add(new LatLng(lat,lng));
                                }
                            }catch (NumberFormatException ex){

                            }

                        }
                        gpsCampaign.setCoverage(latLngs);
                        campaigns.add(gpsCampaign);
                    }
            }
            return campaigns;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void storeValue(String value, final Context context){
        context.getSharedPreferences(SDK_CONFIG_FILE, Context.MODE_PRIVATE).edit()
                .putString("value", value)
                .apply();
    }

    public String getValue(final Context context){
        return context.getSharedPreferences(SDK_CONFIG_FILE, Context.MODE_PRIVATE).getString("value", null);
    }

    /**
     * @return AppName as String
     */
    public String getAppName(final Context context){
        return context.getSharedPreferences(SDK_CONFIG_FILE, Context.MODE_PRIVATE).getString("app", null);
    }

    /**
     * @param name AppName from resources
     */
    public void setAppName(String name, final Context context){
        context.getSharedPreferences(SDK_CONFIG_FILE, Context.MODE_PRIVATE).edit().putString("app", name).apply();
    }

    /* Stores a JSONObject as String into the preferences */
    public void storeDeviceInfo(String info, final Context context){
        context.getSharedPreferences(SDK_CONFIG_FILE, Context.MODE_PRIVATE).edit().putString("device", info).apply();
    }

    /* Gets device info stored */
    public JSONObject getDeviceInfo(final Context context) throws JSONException {
        JSONObject device = null;
        String info =  context.getSharedPreferences(SDK_CONFIG_FILE, Context.MODE_PRIVATE).getString("device", null);
        if (info != null) device = new JSONObject(info);
        return device;
    }

    public void store_tiles(String json, final Context context){
        context.getSharedPreferences(APP_STATUS_PREFS_NAME, Context.MODE_PRIVATE).edit().putString(HOME_TILES, json).apply();
    }

    public JSONArray get_tiles(final Context context){
        String tiles = context.getSharedPreferences(APP_STATUS_PREFS_NAME, Context.MODE_PRIVATE).getString(HOME_TILES, null);

        if (tiles != null) { //if something's stored
            try {
                return new JSONArray(tiles);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

}