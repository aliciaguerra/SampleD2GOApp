package com.digital2go.sdk.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.digital2go.sdk.api.D2GORequests;
import com.digital2go.sdk.domain.GeofencesInArea;
import com.digital2go.sdk.managers.LocationManager;
import com.digital2go.sdk.utils.SDKPreferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * @author Digital2Go
 * This service handles the Active GPS Scanning to check if position is in polygon
 */
public class LocationService extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /** TAG for debug purposes */
    private final String TAG = getClass().getSimpleName();
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        configureService();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
        mGoogleApiClient = null;
    }

    private void configureService() {
        createLocationRequest();
        buildGoogleApiClient();
        connectClient();
    }

    /** Connects the google client */
    private void connectClient() {
        if (mGoogleApiClient != null) mGoogleApiClient.connect();
    }

    /** Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API. */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /** Creates the LocationRequest for constants updates */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest()
                .setInterval(1500)
                .setFastestInterval(1500 / 2)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /** Starts Location Updates */
    public void startLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            if (isLocationPermissionGranted()) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }else LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        }
    }

    /** Stops Location Updates */
    public void stopLocationUpdates(){
        if (mGoogleApiClient != null)
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private boolean isLocationPermissionGranted() {
        int permission = ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
        getGeofencesInArea();
    }

    private void getGeofencesInArea() {
        Location location = getLastLocation();
        LocationManager.getInstance().setLastLocation(location);
        try{
            if (location != null){
                D2GORequests.getInstance().geofenceInRange(new LatLng(location.getLatitude(), location.getLongitude()), 500, getApplicationContext());
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        LocationManager.getInstance().setLastLocation(location);
        final Context context = getApplicationContext();
        Intent loc = new Intent("com.d2go.interaction."+ SDKPreferences.getInstance().getAppName(context).toLowerCase());
        loc.putExtra("_location", true);
        loc.putExtra("_lat", location.getLatitude());
        loc.putExtra("_lng", location.getLongitude());

        getApplicationContext().sendBroadcast(loc);
        if (SDKPreferences.getInstance().getGeofencesList(context).size()>0){
            List<GeofencesInArea> geofencesInArea = SDKPreferences.getInstance().getGeofencesList(context);

            String value = SDKPreferences.getInstance().getValue(context);
            JSONArray jsonArray = null;
            try {
                if (value != null){
                    JSONObject jsonObject = new JSONObject(value);
                    jsonArray = (JSONArray) jsonObject.get("data");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            for(GeofencesInArea campaign : geofencesInArea){
                int index = geofencesInArea.indexOf(campaign);
                switch (campaign.getCoverage_type()){
                    case "polygon":
                        try {
                            if (PolyUtil.containsLocation(new LatLng(location.getLatitude(), location.getLongitude()), campaign.getCoverage(), false)) {
                                if (!campaign.isDisplayed()) {
                                    getApplicationContext().sendBroadcast(new Intent("com.d2go.interaction."+SDKPreferences.getInstance().getAppName(context).toLowerCase()).putExtra("_console", "Entering campaign: "+campaign.getCampaign_id()));
                                    geofenceImpression(campaign.getCampaign_id(), "enter", location);
                                    campaign.setDisplayed(true);
                                }
                            } else {
                                if (campaign.isDisplayed()) {
                                    getApplicationContext().sendBroadcast(new Intent("com.d2go.interaction."+SDKPreferences.getInstance().getAppName(context).toLowerCase()).putExtra("_console", "Exiting campaign: "+campaign.getCampaign_id()));
                                    geofenceImpression(campaign.getCampaign_id(), "exit", location);
                                    campaign.setDisplayed(false);
                                }
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        break;

                    case "radius":
                        try {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject explrObject = jsonArray.getJSONObject(i);

                                if (campaign.getCampaign_id().equals(explrObject.getString("campaign_id")) && explrObject.getString("coverage_type").equals("radius")){
                                    Location location1 = new Location(explrObject.getString("campaign_id"));

                                    JSONObject coverage = (JSONObject) explrObject.get("coverage");
                                    if ( coverage.get("lat") != null && coverage.get("lng") != null){
                                        location1.setLatitude(coverage.getDouble("lat"));
                                        location1.setLongitude(coverage.getDouble("lng"));

                                        double meters = getMeters(coverage.getDouble("radius"));
                                        float distance = location.distanceTo(location1);

                                        if (distance <= meters){
                                            if (!campaign.isDisplayed()){
                                                getApplicationContext().sendBroadcast(new Intent("com.d2go.interaction."+SDKPreferences.getInstance().getAppName(context).toLowerCase()).putExtra("_console", "Entering: "+campaign.getCampaign_id()));
                                                geofenceImpression(campaign.getCampaign_id(), "enter", location);
                                                campaign.setDisplayed(true);
                                            }
                                        }else{
                                            if (campaign.isDisplayed()) {
                                                getApplicationContext().sendBroadcast(new Intent("com.d2go.interaction."+SDKPreferences.getInstance().getAppName(context).toLowerCase()).putExtra("_console", "Exiting campaign: "+campaign.getCampaign_id()));
                                                geofenceImpression(campaign.getCampaign_id(), "exit", location);
                                                campaign.setDisplayed(false);
                                            }
                                        }
                                    }

                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                }


                geofencesInArea.set(index, campaign);
            }

            SDKPreferences.getInstance().updateGeofencesInArea(geofencesInArea, context);
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void geofenceImpression(String campaignId, String proximity, Location location) throws JSONException, UnsupportedEncodingException {
        final Context context = getApplicationContext();
        JSONObject impression = new JSONObject();

        JSONObject deviceInfo = SDKPreferences.getInstance().getDeviceInfo(context); //to complete device info
        deviceInfo.put("altitude", location.getAltitude());
        deviceInfo.put("horizontal_accuracy", location.getAccuracy());
        deviceInfo.put("vertical_accuracy", location.getAccuracy());

        impression.put("device_id", SDKPreferences.getInstance().getAndroidID(context).replace("-", ""));
        impression.put("os", "android");
        impression.put("bluetooth_enabled", 1);
        impression.put("app_name", SDKPreferences.getInstance().getAppName(context));
        impression.put("type", "geofence");
        impression.put("proximity", proximity);
        impression.put("campaign_id", campaignId);
        impression.put("lat", location.getLatitude());
        impression.put("lng", location.getLongitude());
        impression.put("timestamp", getCurrentTime());
        impression.put("demographics", SDKPreferences.getInstance().getDemographics(context));
        impression.put("deviceinfo", deviceInfo);

        getApplicationContext().sendBroadcast(new Intent("com.d2go.interaction."+SDKPreferences.getInstance().getAppName(context).toLowerCase()).putExtra("_console", "Sending Geofence Impression for: "+campaignId));
        getApplicationContext().sendBroadcast(new Intent("com.d2go.interaction."+SDKPreferences.getInstance().getAppName(context).toLowerCase()).putExtra("_console", "Impression body for: "+campaignId + "\n" + impression.toString()));
        D2GORequests.getInstance().geofenceImpression(impression, context);
    }

    private String getCurrentTime(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDate = null;
        Calendar calendar = Calendar.getInstance();

        currentDate = format.format(calendar.getTime());

        return currentDate;
    }

    /**
     * Obtains the last Location registered by the gps client
     */
    public Location getLastLocation(){
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            if (isLocationPermissionGranted())
                return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        return null;
    }

    /**
     * Converts Miles to Meters by the parameter
     * @param miles Miles to convert
     * @return Meters
     */
    public double getMeters(double miles){
        return miles*1609.344;
    }

}