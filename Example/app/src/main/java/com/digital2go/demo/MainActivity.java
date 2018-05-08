package com.digital2go.demo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.digital2go.sdk.D2GOSDK;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.LoggingBehavior;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import com.digital2go.demo.models.Profile;
import com.digital2go.demo.utils.Preferences;
import com.digital2go.demo.utils.ScreenUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    CallbackManager callbackManager;
    LoginButton loginButton;
    Button login;
    public static boolean isLogged = false;

    /** Bluetooth timer task**/
    Timer timer;
    TimerTask timerTask;
    //Handler to run over the timer task
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init facebook sdk
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        FacebookSdk.addLoggingBehavior(LoggingBehavior.REQUESTS);
        setContentView(R.layout.activity_main);
        login = (Button) findViewById(R.id.btn_login);
        login.setOnClickListener(this);
        callbackManager = CallbackManager.Factory.create();
        validateBluetooth(); //before anything, we validate bt


        /** Permission Request **/
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermission()) {
                requestForSpecificPermission();
            }
        }

        if (AccessToken.getCurrentAccessToken() != null) startActivity(new Intent(getApplicationContext(),SecondActivity.class));

        bluetoothTimer();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email","public_profile","user_friends","user_location","user_status");
        loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        stopBluetoothTimer();
                        getFBProfile();
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(MainActivity.this, R.string.facebook_error, Toast.LENGTH_SHORT).show();
                        LoginManager.getInstance().logOut();
                    }
                });
    }

    /**
     * Check if permissions are granted (bluetooth, gps)
     * @return true if so, false otherwise
     */
    private boolean checkIfAlreadyhavePermission() {
        int bluetooth = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH);
        int bluetoothAdmin = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN);
        int coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

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
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION},
                101);
    }

    /**
     * Gets Facebook info from user logged
     */
    private void getFBProfile(){
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                finish();
                try {
                    Profile profile = new Profile();
                    profile.setFirst_name(object.getString("first_name"));
                    profile.setLast_name(object.getString("last_name"));
                    profile.setEmail(object.getString("email"));
                    profile.setSex(object.getString("gender").charAt(0));

                    Preferences.getInstance(getApplicationContext()).saveProfile(profile);
                    Preferences.getInstance(getApplicationContext()).setLogged(true);
                    startActivity(new Intent(getApplicationContext(),SecondActivity.class));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "name,first_name,last_name,email,id,birthday,location,gender");
        request.setParameters(parameters);
        request.executeAsync();
    }

    /**
     * to handle login facebook results
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (Preferences.getInstance(getApplicationContext()).isLogged()) {
                Profile profile = Preferences.getInstance(getApplicationContext()).getProfile();
                if (profile != null && profile.getAge() != 0 && profile.getGender() != null && profile.getCity() != null) {
                    JSONObject demographics = new JSONObject();
                    demographics.put("age", profile.getAge());
                    demographics.put("gender", profile.getGender());
                    demographics.put("city", profile.getCity());


                }
                startActivity(new Intent(this, SecondActivity.class));
            }

            isLogged = false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_login:
                TextView uname = (TextView) findViewById(R.id.txt_username);
                TextView pass = (TextView) findViewById(R.id.txt_password);

                if(!uname.getText().toString().isEmpty() && !pass.getText().toString().isEmpty()){
                    if (uname.getText().toString().equals("guest") && pass.getText().toString().equals("guest"))
                        finish();
                    stopBluetoothTimer();
                    Preferences.getInstance(getApplicationContext()).setLogged(true);
                    startActivity(new Intent(getApplicationContext(), SecondActivity.class));

                }else
                    Toast.makeText(MainActivity.this, R.string.login_error, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * Create, sets and start the timer to keep monitoring the bluetooth adapter
     */
    private void bluetoothTimer(){
        //set the timer
        timer = new Timer();
        //initialize the task
        initializeTimer();
        //schedule timer to keep monitoring
        timer.schedule(timerTask, 500, 800);
    }

    /**
     * Stops the bluetooth timer
     */
    private void stopBluetoothTimer(){
        //if is already started, stop it
        if (timer != null){
            timer.cancel();
            timer = null;
        }
    }

    private void initializeTimer(){
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        validateBluetooth();
                    }
                });
            }
        };
    }

    private void validateBluetooth(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            getApplicationContext().sendBroadcast(new Intent("com.d2go.interaction."+String.valueOf(R.string.app_name).toLowerCase()).putExtra("_console", "Bluetooth not Supported"));
            stopBluetoothTimer();
            return;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enable
                ScreenUtils.getInstance().setScreenColor(getWindow(), ContextCompat.getColor(getApplicationContext(), R.color.colorError));
            }else {
                // if enabled
                ScreenUtils.getInstance().setScreenColor(getWindow(), ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
            }
        }
    }
}
