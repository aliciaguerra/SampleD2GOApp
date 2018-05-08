package com.digital2go.sdk.api;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.InvalidParameterException;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.digital2go.sdk.D2GOSDK;
import com.digital2go.sdk.api.interfaces.API;
import com.digital2go.sdk.managers.LocationManager;
import com.digital2go.sdk.services.GeofenceInAreaService;
import com.digital2go.sdk.services.RefreshService;
import com.digital2go.sdk.utils.DeviceUtils;
import com.digital2go.sdk.utils.SDKPreferences;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedInput;


/**
 * @author Digital2Go
 * Created by Ulises Rosas
 */

public class D2GORequests {
    private String TAG = getClass().getSimpleName(); //For debug purposes
    private static API restAdapter = null;
    private static D2GORequests instance = null;
    private static AmazonSNSClient client;

    /**
     * Create/Retrieve an instance to perform Requests
     * @return an instance of this class
     */
    public static synchronized D2GORequests getInstance(){
        if (instance == null) instance = new D2GORequests();
        return instance;
    }

    /**
     * Authentication with digital2go API to request JWT token
     * @param username Username
     * @param password Password
     */
    public synchronized void login(final String username, final String password) throws JSONException, UnsupportedEncodingException {
        final Context context = D2GOSDK.getAppContext();
        if (context != null){ //only if context != null
            final SDKPreferences preferences = SDKPreferences.getInstance(); //get preferences instance as final
            DeviceUtils.info(context);
            String id = preferences.getAndroidID(context);

            if (id.equals("") || id == null) preferences.storeAndroidID(context);
            id = preferences.getAndroidID(context).replace("-", "");

            TypedInput input = null;
            JsonObject login = new JsonObject();

            login.addProperty("username", username);
            login.addProperty("password", password);
            login.addProperty("device_id", id);

            input = new TypedByteArray("application/json", login.toString().getBytes("UTF-8"));

            String endpoint = context.getString(R.string.d2go_api_endpoint);
            restAdapter = new RestAdapter.Builder().setEndpoint(endpoint).build().create(API.class);
            restAdapter.login(input, new Callback(){

                @Override
                public void success(Object o, Response response) {
                    Intent interaction = new Intent("com.d2go.interaction."+preferences.getAppName(context).toLowerCase());
                    if(response.getStatus() == HttpURLConnection.HTTP_OK){
                        try {
                            String responseString = String.valueOf(getJSON(response.getBody().in()));
                            preferences.storeToken(responseString, context);
                            preferences.ApiConnection(true, context);

                            subscribeSNS(context);

                            context.startService(new Intent(context, RefreshService.class));
                            context.startService(new Intent(context, GeofenceInAreaService.class));

                            JSONObject result = new JSONObject(responseString);
                            get_tiles(result.getString("app_guid"), context); //retrieve tiles from server, with app_guid

                            interaction.putExtra("_login", true);
                            context.sendBroadcast(interaction);
                        } catch (Exception e) {
                            Log.e("Error", e.getMessage());
                            interaction.putExtra("_login", false);
                            context.sendBroadcast(interaction);
                            e.printStackTrace();
                        }
                    }else{
                        SDKPreferences preferences = SDKPreferences.getInstance();
                        preferences.storeToken(null, context);
                        preferences.ApiConnection(false, context);

//                        context.stopService(new Intent(context, RefreshService.class));
                        context.stopService(new Intent(context, GeofenceInAreaService.class));
                        interaction.putExtra("_login", false);
                        context.sendBroadcast(interaction);
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Intent interaction = new Intent("com.d2go.interaction."+preferences.getAppName(context).toLowerCase());
                    preferences.storeToken(null, context);
                    preferences.ApiConnection(false, context);

                    interaction.putExtra("_login", false);
                    context.sendBroadcast(interaction);
                }
            });
        }
    }

    /**
     * Make a beacon impression to the rest api
     * @param impression beacon impression object
     */
    public synchronized void beaconImpression(final JSONObject impression, final Context context) throws JSONException, UnsupportedEncodingException {
        if (context != null){
            final SDKPreferences preferences = SDKPreferences.getInstance();
            Intent interaction = new Intent("com.d2go.interaction."+preferences.getAppName(context).toLowerCase());
            interaction.putExtra("_console", "Impression for beacon: M:"+ impression.getInt("major") + ", m:"+ impression.getInt("minor_dec"));
            context.sendBroadcast(interaction);

            final String beacon = String.valueOf(impression.getInt("minor_dec"));
            if (preferences.getApiConnection(context)){
                final String token = preferences.getToken(context).getString("token");

                if (token != null) {
                    String endpoint = context.getString(R.string.d2go_api_endpoint);
                    restAdapter = new RestAdapter.Builder()
                            .setRequestInterceptor(new RequestInterceptor() {
                                @Override
                                public void intercept(RequestFacade request) {
                                    request.addHeader("Authorization", "Bearer " + token);
                                }
                            })
                            .setEndpoint(endpoint)
                            .build()
                            .create(API.class);

                    TypedInput input = null;
                    input = new TypedByteArray("application/json", impression.toString().getBytes("UTF-8"));

                    restAdapter.beacon_impression(input, new Callback() {

                        /**
                         * Successful HTTP response.
                         *
                         * @param o
                         * @param response
                         */
                        @Override
                        public void success(Object o, Response response) {
                            if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                                JSONObject campaign = null; //campaign as string
                                String id = null;
                                try {
                                    campaign = getJSON(response.getBody().in());
                                    id = campaign.getString("id");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if (campaign != null && id != null){
                                    context.sendBroadcast(createIntent(campaign.toString()).setAction("com.d2go.sdk.beaconimpression." + preferences.getAppName(context).toLowerCase()));
                                    context.sendBroadcast(new Intent("com.d2go.interaction." + preferences.getAppName(context).toLowerCase()).putExtra("_console", "Campaign received: " + id));
                                }
                            }else{
                                context.sendBroadcast(new Intent("com.d2go.interaction." + preferences.getAppName(context).toLowerCase()).putExtra("_console", "Server response: " + response.getBody().toString() + " for beacon: " + beacon));
                            }
                        }

                        /**
                         * Unsuccessful HTTP response due to network failure, non-2XX status code, or unexpected
                         * exception.
                         *
                         * @param error
                         */
                        @Override
                        public void failure(RetrofitError error) {
                            context.sendBroadcast(new Intent("com.d2go.interaction." + preferences.getAppName(context).toLowerCase()).putExtra("_console", "Beacon Impression error: " + error.getMessage() + " for beacon: " + beacon));
                        }
                    });
                }
            }
        }

    }

    /**
     * Make a geofence impression to the rest api
     * @param impression geofence impression object
     */
    public synchronized void geofenceImpression(final JSONObject impression, final Context context) throws JSONException, UnsupportedEncodingException {
        if (context != null){
            final SDKPreferences preferences = SDKPreferences.getInstance();
            Intent interaction = new Intent("com.d2go.interaction."+preferences.getAppName(context).toLowerCase());
            interaction.putExtra("_console", "Impression for geofence: "+ impression.getString("campaign_id"));
            context.sendBroadcast(interaction);

            final String campaign = impression.getString("campaign_id");
            if (preferences.getApiConnection(context)){
                final String token = preferences.getToken(context).getString("token");

                if (token != null) {
                    String endpoint = context.getString(R.string.d2go_api_endpoint);
                    restAdapter = new RestAdapter.Builder()
                            .setRequestInterceptor(new RequestInterceptor() {
                                @Override
                                public void intercept(RequestFacade request) {
                                    request.addHeader("Authorization", "Bearer " + token);
                                }
                            })
                            .setEndpoint(endpoint)
                            .build()
                            .create(API.class);

                    TypedInput input = null;
                    input = new TypedByteArray("application/json", impression.toString().getBytes("UTF-8"));

                    // API Request
                    restAdapter.geofence_impression(input, new Callback() {

                        /**
                         * Successful HTTP response.
                         *
                         * @param o
                         * @param response
                         */
                        @Override
                        public void success(Object o, Response response) {
                            if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                                JSONObject campaign = null; //campaign as string
                                String id = null;
                                try {
                                    campaign = getJSON(response.getBody().in());
                                    id = campaign.getString("id");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if (campaign != null && id != null){
                                    context.sendBroadcast(createIntent(campaign.toString()).setAction("com.d2go.sdk.geofenceimpression." + preferences.getAppName(context).toLowerCase()));
                                    context.sendBroadcast(new Intent("com.d2go.interaction." + preferences.getAppName(context).toLowerCase()).putExtra("_console", "Campaign received: " + id));
                                }
                            }
                            else{
                                context.sendBroadcast(new Intent("com.d2go.interaction." + preferences.getAppName(context).toLowerCase()).putExtra("_console", "Server response: " + response.getBody().toString() + " for geofence: " + campaign));
                            }
                        }

                        /**
                         * Unsuccessful HTTP response due to network failure, non-2XX status code, or unexpected
                         * exception.
                         *
                         * @param error
                         */
                        @Override
                        public void failure(RetrofitError error) {
                            context.sendBroadcast(new Intent("com.d2go.interaction." + preferences.getAppName(context).toLowerCase()).putExtra("_console", "Geofence Impression error: " + error.getMessage() + " for: "+campaign));
                        }
                    });
                }
            }
        }
    }

    /**
     * Request for TokenRefresh
     */
    public synchronized void refreshToken(final Context context) throws JSONException, UnsupportedEncodingException {
        final SDKPreferences preferences = SDKPreferences.getInstance();
        if (preferences.getApiConnection(context)){
            Intent interaction = new Intent("com.d2go.interaction."+preferences.getAppName(context).toLowerCase());
            interaction.putExtra("_console", "Refreshing token");
            context.sendBroadcast(interaction);

            String endpoint = context.getString(R.string.d2go_api_endpoint);
            restAdapter = new RestAdapter.Builder()
                    .setEndpoint(endpoint)
                    .build()
                    .create(API.class);

            final String token = preferences.getToken(context).getString("refresh");
            final String device = preferences.getAndroidID(context);
            if (token != null) {
                JSONObject refresh = new JSONObject();
                refresh.put("device", device);
                refresh.put("token", token);

                TypedInput input = null;
                input = new TypedByteArray("application/json", refresh.toString().getBytes("UTF-8"));

                restAdapter.refresh(input, new Callback(){

                    @Override
                    public void success(Object o, Response response) {
                        if(response.getStatus() == HttpURLConnection.HTTP_OK){
                            try {
                                String responseString = String.valueOf(getJSON(response.getBody().in()));

                                preferences.storeToken(responseString, context);
                                preferences.ApiConnection(true, context);
                                Intent interaction = new Intent("com.d2go.interaction."+preferences.getAppName(context).toLowerCase());
                                interaction.putExtra("_console", "Token updated");
                                context.sendBroadcast(interaction);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }else{
                            SDKPreferences preferences = SDKPreferences.getInstance();
                            preferences.storeToken(null, context);
                            preferences.ApiConnection(false, context);

                            Intent interaction = new Intent("com.d2go.interaction."+preferences.getAppName(context).toLowerCase());
                            interaction.putExtra("_console", "Wrong request, disconnected");
                            context.sendBroadcast(interaction);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        preferences.storeToken(null, context);
                        preferences.ApiConnection(false, context);

                        Intent interaction = new Intent("com.d2go.interaction."+preferences.getAppName(context).toLowerCase());
                        interaction.putExtra("_console", "Refresh error");
                        context.sendBroadcast(interaction);
                    }
                });
            }
        }
    }

    /**
     * Gets geofences in range
     * @param location Current Location
     * @param radius Radius to sense
     */
    public synchronized void geofenceInRange(LatLng location, int radius, final Context context) throws JSONException {
        final SDKPreferences preferences = SDKPreferences.getInstance();
        if (preferences.getApiConnection(context)){
            final String token = preferences.getToken(context).getString("token");

            if (token != null) {
                String endpoint = context.getString(R.string.d2go_api_endpoint);
                restAdapter = new RestAdapter.Builder()
                        .setRequestInterceptor(new RequestInterceptor() {
                            @Override
                            public void intercept(RequestFacade request) {
                                request.addHeader("Authorization", "Bearer " + token);
                            }
                        })
                        .setEndpoint(endpoint)
                        .build()
                        .create(API.class);

                restAdapter.geofenceInRange(location.latitude, location.longitude, radius, new Callback() {

                    /**
                     * Successful HTTP response.
                     *
                     * @param o
                     * @param response
                     */
                    @Override
                    public void success(Object o, Response response) {
                        if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                            try {
                                String value = getJSONArray(response.getBody().in()).toString();
                                preferences.storeValue(value, context);
                                preferences.storeGeofencesInArea(value, context);

                                if (context != null)
                                    context.sendBroadcast(new Intent("com.d2go.interaction." + preferences.getAppName(context).toLowerCase()).putExtra("_console", "Getting geofences in Area"));
                            } catch (Exception e) {
                                if (context != null)
                                    context.sendBroadcast(new Intent("com.d2go.interaction." + preferences.getAppName(context).toLowerCase()).putExtra("_console", "Geofences in area error " + e.getLocalizedMessage()));
                                e.printStackTrace();
                            }
                        }
                    }

                    /**
                     * Unsuccessful HTTP response due to network failure, non-2XX status code, or unexpected
                     * exception.
                     *
                     * @param error
                     */
                    @Override
                    public void failure(RetrofitError error) {
                        context.sendBroadcast(new Intent("com.d2go.interaction." + preferences.getAppName(context).toLowerCase()).putExtra("_console", "Geofences in area error " + error.getLocalizedMessage()));
                        error.printStackTrace();
                    }
                });
            }
        }
    }

    /**
     * Changes device id, and gets new data
     * @param deviceId Current device id
     * @throws JSONException
     */
    public synchronized void change_deviceId(String deviceId, final Context context) throws JSONException {
        final SDKPreferences preferences = SDKPreferences.getInstance();
        if (preferences.getApiConnection(context)){
            final String token = preferences.getToken(context).getString("token");

            if (token != null) {
                String endpoint = context.getString(R.string.d2go_api_endpoint);
                restAdapter = new RestAdapter.Builder()
                        .setRequestInterceptor(new RequestInterceptor() {
                            @Override
                            public void intercept(RequestFacade request) {
                                request.addHeader("Authorization", "Bearer " + token);
                            }
                        })
                        .setEndpoint(endpoint)
                        .build()
                        .create(API.class);

                restAdapter.change_deviceId(deviceId, new Callback() {
                    @Override
                    public void success(Object o, Response response) {
                        Intent interaction = new Intent("com.d2go.interaction." + preferences.getAppName(context).toLowerCase());
                        if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                            try {
                                String responseString = String.valueOf(getJSON(response.getBody().in()));

                                preferences.storeToken(responseString, context);
                                preferences.ApiConnection(true, context);

                                context.startService(new Intent(context, RefreshService.class));
                                context.startService(new Intent(context, GeofenceInAreaService.class));

                                interaction.putExtra("_login", true);
                                context.sendBroadcast(interaction);
                            } catch (Exception e) {
                                interaction.putExtra("_login", false);
                                context.sendBroadcast(interaction);
                                e.printStackTrace();
                            }
                        } else {
                            SDKPreferences preferences = SDKPreferences.getInstance();
                            preferences.storeToken(null, context);
                            preferences.ApiConnection(false, context);

                            context.stopService(new Intent(context, RefreshService.class));
                            context.stopService(new Intent(context, GeofenceInAreaService.class));
                            interaction.putExtra("_login", false);
                            context.sendBroadcast(interaction);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Intent interaction = new Intent("com.d2go.interaction." + preferences.getAppName(context).toLowerCase());
                        preferences.storeToken(null, context);
                        preferences.ApiConnection(false, context);

                        interaction.putExtra("_login", false);
                        context.sendBroadcast(interaction);
                    }
                });
            }
        }
    }

    /**
     * Registers interaction to the api
     * @param campaign campain interaction
     * @throws UnsupportedEncodingException
     */
    public synchronized void register_interaction(JSONObject campaign, final Context context) throws UnsupportedEncodingException, JSONException {
        final SDKPreferences preferences = SDKPreferences.getInstance();
        if (preferences.getApiConnection(context)){
            TypedInput input = null;
            if (campaign != null){

                JSONObject interaction = new JSONObject();

                interaction.put("campaign_id", campaign.getString("id"));
                interaction.put("impression_id", campaign.getString("impression_id"));
                String id = preferences.getAndroidID(context);

                if (id.equals("") || id == null) preferences.storeAndroidID(context);
                id = preferences.getAndroidID(context).replace("-", "");
                interaction.put("device_id", id);
                interaction.put("action", getCampaignAction(campaign));
                interaction.put("timestamp", getCurrentTime());

                input = new TypedByteArray("application/json", interaction.toString().getBytes("UTF-8"));

                final String token = preferences.getToken(context).getString("token");

                if (token != null) {
                    String endpoint = context.getString(R.string.d2go_api_endpoint);
                    restAdapter = new RestAdapter.Builder()
                            .setRequestInterceptor(new RequestInterceptor() {
                                @Override
                                public void intercept(RequestFacade request) {
                                    request.addHeader("Authorization", "Bearer " + token);
                                }
                            })
                            .setEndpoint(endpoint)
                            .build()
                            .create(API.class);

                    restAdapter = new RestAdapter.Builder().setEndpoint(endpoint).build().create(API.class);
                    restAdapter.register_interaction(input, new Callback() {
                        @Override
                        public void success(Object o, Response response) {

                        }

                        @Override
                        public void failure(RetrofitError error) {
                            context.sendBroadcast(new Intent("com.d2go.interaction." + preferences.getAppName(context).toLowerCase()).putExtra("_console", "Interaction Error " + error.getLocalizedMessage()));
                        }
                    });
                }
            }
        }
    }

    public synchronized void track_user(final Context context) throws JSONException, UnsupportedEncodingException {
        final SDKPreferences preferences = SDKPreferences.getInstance();
        if (preferences.getApiConnection(context)){
            Location location = LocationManager.getInstance().getLastLocation();
            if (location != null){
                TypedInput input = null;
                JSONObject track = new JSONObject();

                track.put("device_id", preferences.getAndroidID(context).replace("-", ""));
                track.put("app_name", preferences.getAppName(context));
                track.put("os", "android");
                track.put("bluetooth_enabled", 1);
                track.put("type", "track");
                track.put("lat", location.getLatitude());
                track.put("lng", location.getLongitude());
                track.put("demographics", preferences.getDemographics(context));
                track.put("timestamp", getCurrentTime());


                input = new TypedByteArray("application/json", track.toString().getBytes("UTF-8"));

                String endpoint = context.getString(R.string.d2go_api_endpoint);
                restAdapter = new RestAdapter.Builder().setEndpoint(endpoint).build().create(API.class);
                restAdapter.track(input, new Callback(){

                    @Override
                    public void success(Object o, Response response) {

                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });
            }
        }
    }

    public synchronized void get_tiles(String app_guid, final Context context) throws JSONException {
        final SDKPreferences preferences = SDKPreferences.getInstance();
        if (preferences.getApiConnection(context)){
            final String token = preferences.getToken(context).getString("token");

            if (token != null) {
                String endpoint = context.getString(R.string.d2go_api_endpoint);
                restAdapter = new RestAdapter.Builder()
                        .setRequestInterceptor(new RequestInterceptor() {
                            @Override
                            public void intercept(RequestFacade request) {
                                request.addHeader("Authorization", "Bearer " + token);
                            }
                        })
                        .setEndpoint(endpoint)
                        .build()
                        .create(API.class);

                restAdapter.get_tiles(app_guid, new Callback() {
                    @Override
                    public void success(Object o, Response response) {
                        if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                            try {
                                JSONArray tiles = getTiles(response.getBody().in());
                                preferences.store_tiles(tiles.toString(), context);
                            } catch (Exception e) {
                                preferences.store_tiles(null, context);
                                e.printStackTrace();
                            }
                        } else {
                            preferences.store_tiles(null, context);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        preferences.store_tiles(null, context);
                        error.printStackTrace();
                    }
                });
            }

        }
    }

    private static JSONObject getJSON(InputStream in) throws IOException, JSONException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        String line;
        JSONObject jsonObject = null;

        while ((line = reader.readLine()) != null) {
            out.append(line);
            out.append(newLine);
        }
        jsonObject = new JSONObject(out.toString()).getJSONObject("data");
        return jsonObject;
    }

    private static JSONObject getJSONArray(InputStream in) throws IOException, JSONException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        String line;
        JSONObject jsonObject = null;

        while ((line = reader.readLine()) != null) {
            out.append(line);
            out.append(newLine);
        }
        jsonObject = new JSONObject(out.toString());

        return jsonObject;
    }

    private static JSONArray getTiles(InputStream in) throws IOException, JSONException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        String line;
        JSONArray jsonObject = null;

        while ((line = reader.readLine()) != null) {
            out.append(line);
            out.append(newLine);
        }
        jsonObject = new JSONObject(out.toString()).getJSONArray("data");

        return jsonObject;
    }

    /**
     * Creates intent to broadcast
     * @return Intent within campaign
     */
    private Intent createIntent(String campaign) {
        Intent intent = new Intent();
        intent.putExtra("campaign", campaign);
        return intent;
    }

    private String getCurrentTime(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDate = null;
        Calendar calendar = Calendar.getInstance();

        currentDate = format.format(calendar.getTime());

        return currentDate;
    }

    private String getCampaignAction(JSONObject campaign){
        String action = null;
        try {
            if (campaign.getJSONObject("campaign_content") != null && campaign.getJSONObject("campaign_content").getJSONArray("campaign_content_buttons").length() > 0) {
                JSONArray contentButtons = campaign.getJSONObject("campaign_content").getJSONArray("campaign_content_buttons");
                JSONObject actions = contentButtons.getJSONObject(0);

                action = actions.getString("action");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return action;
    }

    public synchronized static void subscribeSNS(final Context context){
        if (context != null) {
            final SDKPreferences preferences = SDKPreferences.getInstance();

            String aws_key = context.getString(R.strings.aws_key);
            String aws_secret = context.getString(R.strings.aws_secret);

            AWSCredentials awsCredentials = new BasicAWSCredentials(aws_key, aws_secret);
            client = new AmazonSNSClient(awsCredentials);
            client.setRegion(Region.getRegion(Regions.US_WEST_2));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String endpoint = createEndpoint(preferences.getFirebaseToken(context), preferences.getToken(context).getString("android_arn"));
                        SubscribeRequest subscribeRequest = new SubscribeRequest()
                                .withTopicArn(preferences.getToken(context).getString("sns"))
                                .withProtocol("application")
                                .withEndpoint(endpoint);

                        SubscribeResult result = client.subscribe(subscribeRequest);
                        Log.i("Notification", "Subscription: " + result.getSubscriptionArn());
                    } catch (Exception e) {
                        Log.e("Notification", "Error: " + e.getMessage());
                    }
                }
            }).start();
        }
    }

    private static String createEndpoint(String token, String arn) {

        String endpointArn = null;
        try {
            CreatePlatformEndpointRequest cpeReq =
                    new CreatePlatformEndpointRequest()
                            .withPlatformApplicationArn(arn)
                            .withToken(token);
            CreatePlatformEndpointResult cpeRes = client
                    .createPlatformEndpoint(cpeReq);
            endpointArn = cpeRes.getEndpointArn();
        } catch (InvalidParameterException ipe) {
            Log.e("Error", ipe.getMessage());
            String message = ipe.getErrorMessage();
            ipe.printStackTrace();
            Pattern p = Pattern
                    .compile(".*Endpoint (arn:aws:sns[^ ]+) already exists " +
                            "with the same token.*");
            Matcher m = p.matcher(message);
            if (m.matches()) {
                // The platform endpoint already exists for this token, but with
                // additional custom data that
                // createEndpoint doesn't want to overwrite. Just use the
                // existing platform endpoint.
                endpointArn = m.group(1);
            } else {
                // Rethrow the exception, the input is actually bad.
                throw ipe;
            }
        }
        return endpointArn;
    }

}