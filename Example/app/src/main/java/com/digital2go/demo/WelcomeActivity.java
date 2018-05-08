package com.digital2go.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.digital2go.sdk.D2GOSDK;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.appevents.AppEventsLogger;

import com.digital2go.demo.utils.Preferences;

public class WelcomeActivity extends AppCompatActivity {
    private Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        preferences = Preferences.getInstance(getApplicationContext()); //retrieve preferences instance
        facebookInitialTasks(); //facebook tasks
        checkPermissions();/** Permission Request **/

        if(!D2GOSDK.isConnected(getApplicationContext())) D2GOSDK.init(getApplicationContext(), preferences.getFirebaseToken());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AccessToken.getCurrentAccessToken() != null || preferences.isLogged()) { //if already logged (redirects to main)
            startActivity(new Intent(getApplicationContext(), MainActivityNew.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }// otherwise, continue...
    }

    private void facebookInitialTasks(){
        FacebookSdk.sdkInitialize(getApplicationContext());
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        FacebookSdk.addLoggingBehavior(LoggingBehavior.REQUESTS);
    }

    private void checkPermissions(){
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermission()) {
                requestForSpecificPermission();
            }
        }
    }

    /**
     * Check if permissions are granted (bluetooth, gps)
     * @return true if so, false otherwise
     */
    private boolean checkIfAlreadyhavePermission() {
        int bluetooth = ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH);
        int bluetoothAdmin = ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN);
        int coarse = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int fine = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);

        if (bluetooth == PackageManager.PERMISSION_GRANTED && bluetoothAdmin == PackageManager.PERMISSION_GRANTED && coarse == PackageManager.PERMISSION_GRANTED && fine == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Makes a permissions request
     */
    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        android.Manifest.permission.BLUETOOTH,
                        android.Manifest.permission.BLUETOOTH_ADMIN,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION},
                101);
    }

    /**
     * Sign In Activity
     * @param view SignIn Button
     */
    public void signIn(View view){
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), SignInActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.to_left, R.anim.fade_out);
    }

    /**
     * Sign Up Activity
     * @param view SignUp Button
     */
    public void signUp(View view){
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), SignUpActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.to_left, R.anim.fade_out);
    }
}
