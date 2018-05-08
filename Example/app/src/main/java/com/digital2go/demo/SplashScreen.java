package com.digital2go.demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.digital2go.sdk.D2GOSDK;
import com.google.firebase.iid.FirebaseInstanceId;
/**
 * Splash Screen activity
 */
public class SplashScreen extends AppCompatActivity {
    private final Handler mHideHandler = new Handler();
    private String firebaseToken = null; //firebase token for push notifications
    private final Runnable mCloseSplash = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), WelcomeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spash_screen);
        firebaseToken = FirebaseInstanceId.getInstance().getToken(); //get firebase token
        Preferences.getInstance(getApplicationContext()).storeFirebaseToken(firebaseToken);

        D2GOSDK.init(getApplicationContext(), Preferences.getInstance(getApplicationContext()).getFirebaseToken()); // initialize d2go sdk
    }

    @Override
    protected void onResume() {
        super.onResume();
        splashTimeOut(3500);
    }

    /**
     * Schedules a call to finish activity, canceling any
     * previously scheduled calls.
     */
    private void splashTimeOut(int delayMillis) {
        mHideHandler.postDelayed(mCloseSplash, delayMillis);
    }
}
