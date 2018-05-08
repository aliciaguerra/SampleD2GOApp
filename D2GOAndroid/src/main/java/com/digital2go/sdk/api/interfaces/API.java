package com.digital2go.sdk.api.interfaces;

import org.json.JSONObject;

import java.lang.reflect.Type;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedInput;

/**
 * @author Digital2Go
 * Created by Ulises Rosas on 12/10/2016.
 */
public interface API {
    @Headers({"Content-Type: application/json"})
    @POST("/apps/login.json")
    void login(@Body TypedInput body, Callback<Response> response);

    @Headers({"Content-Type: application/json"})
    @POST("/impressions/beacon.json")
    void beacon_impression(@Body TypedInput body, Callback<Response> response);

    @Headers({"Content-Type: application/json"})
    @POST("/impressions/geofence.json")
    void geofence_impression(@Body TypedInput body, Callback<Response> response);

    @GET("/geofences/inRange.json")
    void geofenceInRange(@Query("lat") double lat, @Query("lng") double lng, @Query("radius") int radius, Callback<Response> response);

    @Headers({"Content-Type: application/json"})
    @POST("/apps/login.json")
    void refresh(@Body TypedInput body, Callback<Response> response);

    @Headers({"Content-Type: application/json"})
    @POST("/impressions/track.json")
    void track(@Body TypedInput body, Callback<Response> response);

    @Headers({"Content-Type: application/json"})
    @PUT("/devices/edit/{device_id}.json")
    void change_deviceId(@Path("device_id") String deviceId, Callback<Response> response);

    @Headers({"Content-Type: application/json"})
    @POST("/interactions/register.json")
    void register_interaction(@Body TypedInput body, Callback<Response> response);

    @Headers({"Content-Type: application/json"})
    @GET("/apps/screens/{app_guid}.json")
    void get_tiles(@Path("app_guid") String app_guid, Callback<Response> response);
}
