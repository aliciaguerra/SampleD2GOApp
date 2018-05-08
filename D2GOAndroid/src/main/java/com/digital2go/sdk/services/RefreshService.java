package com.digital2go.sdk.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.digital2go.sdk.api.D2GORequests;
import com.digital2go.sdk.utils.SDKPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * @author Digital2Go
 * Service to update token from d2go api before the current token expires
 */
public class RefreshService extends Service {

    private Handler handler = new Handler();
    private Runnable task = new Runnable() {
        @Override
        public void run() {
            try {
                refreshUpdate();
                JSONObject token = SDKPreferences.getInstance().getToken(getApplicationContext());
                if (token != null) handler.postDelayed(this, (long) token.getLong("expiry") / 2);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        update();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        handler.removeCallbacks(task);
    }

    private void update(){
        handler.postDelayed(task, 200000);
    }

    /** Updates token with refresh */
    private void refreshUpdate() throws JSONException, UnsupportedEncodingException {
        D2GORequests.getInstance().refreshToken(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
