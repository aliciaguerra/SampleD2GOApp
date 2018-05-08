package com.digital2go.demo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.digital2go.sdk.D2GOSDK;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;

import java.util.Timer;
import java.util.TimerTask;

import com.digital2go.demo.adapters.ViewPagerAdapter;
import com.digital2go.demo.fragments.FragmentHome;
import com.digital2go.demo.fragments.FragmentOffers;
import com.digital2go.demo.fragments.FragmentSearch;
import com.digital2go.demo.fragments.FragmentSettings;
import com.digital2go.demo.models.Profile;
import com.digital2go.demo.utils.Preferences;
import com.digital2go.demo.utils.ScreenUtils;

public class MainActivityNew extends AppCompatActivity {
    /** Appbar & toolbar **/
    private Toolbar toolbar;
    private TabLayout tabLayout;
    public static ViewPager viewPager;
    /** end Appbar & Toolbar **/

    private static SlidingUpPanelLayout layout; // layout for debug console
    /** Debug console UI **/
    private static TextView console_lat, console_lng, console_interactions, console_status;
    private Button console_clear;
    private static RelativeLayout console;
    /** End Debug console UI **/

    private static Activity activity;

    private int[] tabIcons = {R.drawable.ic_home_white_24dp, R.drawable.magnify, R.drawable.tag, R.drawable.check};

    Timer timer;
    TimerTask timerTask;
    final Handler handler = new Handler();

    public static boolean isLogged = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        if(!D2GOSDK.isConnected(getApplicationContext())) D2GOSDK.init(getApplicationContext(), Preferences.getInstance(getApplicationContext()).getFirebaseToken());
        activity = this;
        configAppbar(); //create and config appbar & toolbar
        layout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        layout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        configDebugConsole();//create and config debug console

        /** Validate if already got a profile **/
        Profile profile = Preferences.getInstance(getApplicationContext()).getProfile();
        if (profile == null || profile.getCity() == null || profile.getAge() == 0 || profile.getSex() == ' ')
            viewPager.setCurrentItem(3, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            JSONArray campaigns = Preferences.getInstance(getApplicationContext()).getCampaignsFound();
//            FragmentSearch.setCampaignAdapter(campaigns);
            startTimer();
            validateBluetooth();
        }catch (Exception e){
//            CrashReporter.logException(e);
            e.printStackTrace();
        }
    }



    private void configDebugConsole() {
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
                ScreenUtils.getInstance().copyClipboard(getApplicationContext(), console_interactions.getText().toString());
                return true;
            }
        });
    }

    private void configAppbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().hide();

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(4);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        setTabLayoutIcons();
    }

    private void setTabLayoutIcons() {
        TextView tabHome = (TextView) LayoutInflater.from(this).inflate(R.layout.tab_custom, null);
        tabHome.setText(R.string.menu_home);
        tabHome.setCompoundDrawablesWithIntrinsicBounds(0, tabIcons[0], 0, 0);
        tabLayout.getTabAt(0).setCustomView(tabHome);

        TextView tabOne = (TextView) LayoutInflater.from(this).inflate(R.layout.tab_custom, null);
        tabOne.setText(R.string.menu_search);
        tabOne.setCompoundDrawablesWithIntrinsicBounds(0, tabIcons[1], 0, 0);
        tabLayout.getTabAt(1).setCustomView(tabOne);

        TextView tabTwo = (TextView) LayoutInflater.from(this).inflate(R.layout.tab_custom, null);
        tabTwo.setText(R.string.menu_offers);
        tabTwo.setCompoundDrawablesWithIntrinsicBounds(0, tabIcons[2], 0, 0);
        tabLayout.getTabAt(2).setCustomView(tabTwo);

        TextView tabThree = (TextView) LayoutInflater.from(this).inflate(R.layout.tab_custom, null);
        tabThree.setText(R.string.menu_settings);
        tabThree.setCompoundDrawablesWithIntrinsicBounds(0, tabIcons[3], 0, 0);
        tabLayout.getTabAt(3).setCustomView(tabThree);
    }

    public void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentHome(), "Home");
        adapter.addFragment(new FragmentSearch(), "Search");
        adapter.addFragment(new FragmentOffers(), "Offers");
        adapter.addFragment(new FragmentSettings(), "Settings");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public static synchronized void setConsole(boolean b){
        if (b) {
            layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
        else layout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    public static void logout(){
        if (AccessToken.getCurrentAccessToken() != null) LoginManager.getInstance().logOut();
        Preferences.getInstance(activity).setLogged(false);
        activity.onBackPressed();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stoptimertask();
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
                ScreenUtils.getInstance().setScreenColor(getWindow(), ContextCompat.getColor(getApplicationContext(), R.color.pure_black));
            }
        }
    }

}
