package com.digital2go.demo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.digital2go.sdk.D2GOSDK;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import com.digital2go.demo.adapters.ViewPagerAdapter;
import com.digital2go.demo.fragments.FragmentOffers;
import com.digital2go.demo.fragments.FragmentSearch;
import com.digital2go.demo.fragments.FragmentSettings;
import com.digital2go.demo.models.Profile;
import com.digital2go.demo.utils.Preferences;
import com.digital2go.demo.utils.ScreenUtils;

public class SecondActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TabLayout tabLayout;
    public static ViewPager viewPager;
    private static Activity activity;
    private static Context context;
    public static boolean debug = false;
    private static SlidingUpPanelLayout layout;
    private String firebaseToken = null;

    private int[] tabIcons = {R.drawable.ic_home_white_24dp, R.drawable.magnify, R.drawable.tag, R.drawable.check};

    public static boolean enabled = false;

    private static TextView console_lat, console_lng, console_interactions, console_status;
    private Button console_clear;
    private static RelativeLayout console;

    Timer timer;
    TimerTask timerTask;
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        FacebookSdk.sdkInitialize(getApplicationContext());
        activity = this;
        context = getApplicationContext();
        firebaseToken = FirebaseInstanceId.getInstance().getToken(); //get firebase token

        layout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        layout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        console = (RelativeLayout) findViewById(R.id.console);
        console_status = (TextView) findViewById(R.id.console_status);
        console_lat = (TextView) findViewById(R.id.console_lat);
        console_lng = (TextView) findViewById(R.id.console_lng);
        console_interactions = (TextView) findViewById(R.id.console_interactions);
        console_clear = (Button) findViewById(R.id.console_clear);
        console_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                console_interactions.setText(null);
            }
        });

        console_interactions.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                copyClipboard(getApplicationContext(), console_interactions.getText().toString());
                return true;
            }
        });
        configUI();
        facebookAuth();

        Profile profile = Preferences.getInstance(getApplicationContext()).getProfile();

//        if (profile == null || profile.getCity() == null || profile.getAge() == 0 || profile.getSex() == ' ')
//            viewPager.setCurrentItem(2, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        enabled = true;
        try {
            D2GOSDK.init(getApplicationContext(), firebaseToken); // initialize d2go sdk
            debug = D2GOSDK.isConnected(getApplicationContext());
        }catch (Exception e){
            e.printStackTrace();
        }


        try {
            JSONArray campaigns = Preferences.getInstance(context).getCampaignsFound();
            FragmentSearch.setCampaignAdapter(campaigns);
//            startTimer();
//            validateBluetooth();
        }catch (Exception e){
//            CrashReporter.logException(e);
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stoptimertask();
    }

    /**
     * This method makes a fb api request for user attrs
     */
    private void facebookAuth(){
        if (AccessToken.getCurrentAccessToken() != null){
            GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
                    try {
                        if (Preferences.getInstance(context).getProfile() == null){
                            Profile profile = new Profile();
                            profile.setFirst_name(object.getString("name"));
                            profile.setSex(object.getString("gender").charAt(0));

                            Preferences.getInstance(context).saveProfile(profile);
                            viewPager.setCurrentItem(2, true);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "name,id,birthday,location,gender");
            request.setParameters(parameters);
            request.executeAsync();
        }
    }

    /**
     * This method gets the values from UI variables
     */
    public void configUI(){

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().hide();

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(2);
        setupViewPager(viewPager);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
//                if (position < 2){
//                    FragmentSettings.hideKeyboard();
//                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        setTabLayoutIcons();
    }


    /**
     * Sets Tab Text and Icon
     */
    private void setTabLayoutIcons() {
        TextView tabHome = (TextView) LayoutInflater.from(this).inflate(R.layout.tab_custom, null);
        tabHome.setText(R.string.menu_home);
        tabHome.setCompoundDrawablesWithIntrinsicBounds(tabIcons[0], 0, 0, 0);
        tabLayout.getTabAt(0).setCustomView(tabHome);

        TextView tabOne = (TextView) LayoutInflater.from(this).inflate(R.layout.tab_custom, null);
        tabOne.setText(R.string.menu_search);
        tabOne.setCompoundDrawablesWithIntrinsicBounds(tabIcons[1], 0, 0, 0);
        tabLayout.getTabAt(0).setCustomView(tabOne);

        TextView tabTwo = (TextView) LayoutInflater.from(this).inflate(R.layout.tab_custom, null);
        tabTwo.setText(R.string.menu_offers);
        tabTwo.setCompoundDrawablesWithIntrinsicBounds(tabIcons[2], 0, 0, 0);
        tabLayout.getTabAt(1).setCustomView(tabTwo);

        TextView tabThree = (TextView) LayoutInflater.from(this).inflate(R.layout.tab_custom, null);
        tabThree.setText(R.string.menu_settings);
        tabThree.setCompoundDrawablesWithIntrinsicBounds(tabIcons[3], 0, 0, 0);
        tabLayout.getTabAt(2).setCustomView(tabThree);
    }

    /**
     * Creates and inserts the fragments into ViewPager
     * @param viewPager
     */
    public void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentSearch(), "Search");
        adapter.addFragment(new FragmentOffers(), "Offers");
        adapter.addFragment(new FragmentSettings(), "Settings");
        viewPager.setAdapter(adapter);
    }

    /**
     * Logouts from the app
     */
    public static void logout(){
        if (AccessToken.getCurrentAccessToken() != null){
            LoginManager.getInstance().logOut();
            activity.finish();
            context.startActivity(new Intent(activity, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }else{
            activity.finish();
            context.startActivity(new Intent(activity, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        Preferences.getInstance(context).setLogged(false);
    }

    /**
     * Take care of calling onBackPressed() for pre-Eclair platforms.
     *
     * @param keyCode
     * @param event
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
        {
            this.moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void copyClipboard(Context context, String text) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
        Toast.makeText(getApplicationContext(), "Copied to Clipboard", Toast.LENGTH_SHORT).show();

    }

    public static synchronized void setConsole(boolean b){
        if (b) {
            layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
        else layout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    public static synchronized void setLocation(String lat, String lng){
        if (console_lat != null) console_lat.setText(lat);
        if (console_lng != null) console_lng.setText(lng);
    }

    public static synchronized void addInteraction(String log){
        if (console_interactions != null){
            String last = console_interactions.getText().toString();

            console_interactions.setText(log + "\n" + last);
        }
    }

    public void startTimer() {
        if (timer == null) {
            //set a new Timer
            timer = new Timer();

            //initialize the TimerTask's job
            initializeTimerTask();

            //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
            timer.schedule(timerTask, 500, 1000); //
        }
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        validateBluetooth();
                        if (D2GOSDK.isConnected(getApplicationContext())){
                            if  (console_status != null) console_status.setText("Connected");
                        }else if (console_status != null) console_status.setText("Disconnected");
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
            ScreenUtils.getInstance().setScreenColor(getWindow(), ContextCompat.getColor(getApplicationContext(), R.color.colorError));
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