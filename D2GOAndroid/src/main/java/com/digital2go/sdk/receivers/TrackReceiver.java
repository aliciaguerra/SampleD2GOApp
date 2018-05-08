package com.digital2go.sdk.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.digital2go.sdk.api.D2GORequests;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;

/**
 * @author Digital2Go
 * Created by Ulises on 24/11/2017.
 */

public class TrackReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            D2GORequests.getInstance().track_user(context);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
