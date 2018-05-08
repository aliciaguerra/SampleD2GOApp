package com.digital2go.demo.fragments;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.digital2go.sdk.D2GOSDK;
import com.digital2go.sdk.exceptions.LoginException;
import com.digital2go.sdk.exceptions.SDKException;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.digital2go.demo.GridSpacingItemDecoration;
import com.digital2go.demo.MainActivityNew;
import com.digital2go.demo.R;
import com.digital2go.demo.adapters.CampaignAdapter;
import com.digital2go.demo.models.Profile;
import com.digital2go.demo.utils.Preferences;
import com.digital2go.demo.utils.ScreenUtils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


/**
 * Created by Ulrick on 10/10/2016.
 */
public class FragmentSearch extends Fragment implements OnClickListener{

    private static AVLoadingIndicatorView loader;
    private static Button search;
    private static TextView message;
    private ImageView background;
    private static FragmentActivity activityFragment;
    private static RelativeLayout scanning;

    public static boolean isLooking;
    private RecyclerView recyclerView;
    private static CampaignAdapter campaignAdapter;
    private static Activity activity;

    public FragmentSearch() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityFragment = getActivity();
        validateBluetooth();
        MainActivityNew.isLogged = true;
        activity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Profile profile = Preferences.getInstance(getContext()).getProfile();

        if (profile != null && profile.getAge() != 0 && profile.getSex() != ' ' && profile.getCity() != null){
            String gender = null;
            switch (profile.getSex()){
                case 'm':
                    gender = "male";
                    break;

                case 'f':
                    gender = "female";
                    break;
            }

            if (!D2GOSDK.isConnected(getContext())) {
                try {
                    JSONObject demographics = new JSONObject();
                    demographics.put("age", profile.getAge());
                    demographics.put("gender", gender);
                    demographics.put("city", profile.getCity());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        configRecycler(view);
        configUI(view);

    }

    public static void newCampaign(String campaign){

        if (campaign != null && campaignAdapter != null){
            if (isLooking){
                loader.setVisibility(GONE);
                message.setVisibility(GONE);
                scanning.setVisibility(VISIBLE);
            }

            campaignAdapter.addCampaign(campaign);
        }

    }

    public static void setCampaignAdapter(JSONArray campaigns){
        if (campaignAdapter != null){
            if (isLooking && campaigns.length() > 0){
                loader.smoothToHide();
                scanning.setVisibility(VISIBLE);
                message.setText("");
            }
            for (int i = 0; i<campaigns.length(); i++){
                try {
                    campaignAdapter.addCampaign(campaigns.getJSONObject(i).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Configs the recyclerview with the custom adapter for campaigns
     * @param view
     */
    private void configRecycler(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        campaignAdapter = new CampaignAdapter(getContext());
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(campaignAdapter);
    }

    /**
     * Configs the UI elements
     * @param view View
     */
    private void configUI(View view) {
        scanning = (RelativeLayout) view.findViewById(R.id.panel_scanning);
        background = (ImageView) view.findViewById(R.id.background);
        loader = (AVLoadingIndicatorView) view.findViewById(R.id.loader);
        loader.smoothToHide();

        updateBackground();

        search = (Button) view.findViewById(R.id.search);
        search.setOnClickListener(this);
        message = (TextView) view.findViewById(R.id.message);

        if (D2GOSDK.isConnected(getContext())){

            if (D2GOSDK.locationServiceStatus() || D2GOSDK.beaconServiceStatus()){
                isLooking = true;
                search.setText(R.string.stop_search);

                if (campaignAdapter.getItemCount() > 0 ){
                    message.setText("");
                    loader.smoothToHide();
                    scanning.setVisibility(VISIBLE);
                }else{
                    message.setText(R.string.searching);
                    loader.refreshDrawableState();
                    loader.smoothToShow();
                }

                try{
                    if (D2GOSDK.locationServiceStatus()) D2GOSDK.startLocationService();
                    if (D2GOSDK.beaconServiceStatus()) D2GOSDK.startBeaconService();
                } catch (LoginException e) {
                    e.printStackTrace();
                } catch (SDKException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e){
                    Toast.makeText(getContext(), R.string.complete_profile, Toast.LENGTH_SHORT).show();
                }
            }else{
                isLooking = false;

                search.setText(R.string.start_search);
                message.setText("");
                loader.smoothToHide();
                scanning.setVisibility(GONE);

                D2GOSDK.stopLocationService();
                D2GOSDK.stopBeaconService();
            }

        }else{
            Profile profile = Preferences.getInstance(getContext()).getProfile();
            if (profile != null  && profile.getAge() != 0 && profile.getGender() != null && profile.getCity() != null) {
                try {
                    JSONObject demographics = new JSONObject();
                    demographics.put("age", profile.getAge());
                    demographics.put("gender", profile.getGender());
                    demographics.put("city", profile.getCity());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            search.setText(R.string.start_search);
            message.setText("");
            loader.smoothToHide();

            D2GOSDK.stopLocationService();
            D2GOSDK.stopBeaconService();

        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if(isLooking){
            if(campaignAdapter.getItemCount() == 0){
                loader.smoothToHide();
                message.setText(R.string.search_fail);
            }else{
                message.setText("");
            }
            search.setText(R.string.start_search);
            scanning.setVisibility(GONE);

            Preferences.getInstance(getContext()).clearCampaignsFound();

            D2GOSDK.stopBeaconService();
            D2GOSDK.stopLocationService();
            isLooking = false;
        }else{
            if (D2GOSDK.isConnected(getContext())){
                if (D2GOSDK.getDemographics(getContext()) != null) {
                    campaignAdapter.restartCampaigns();
                    if (campaignAdapter.getItemCount() == 0) {
                        loader.smoothToShow();
                        message.setVisibility(VISIBLE);
                        message.setText(R.string.searching);
                    }

                    search.setText(R.string.stop_search);

                    try {
                        D2GOSDK.startBeaconService();
                        D2GOSDK.startLocationService();
                    } catch (LoginException e) {
                        e.printStackTrace();
                    } catch (SDKException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e){
                        Toast.makeText(getContext(), R.string.complete_profile, Toast.LENGTH_SHORT).show();
                    }

                    isLooking = true;
                }else
                    Toast.makeText(activityFragment, "Please complete your profile at Settings", Toast.LENGTH_SHORT).show();
            }else {
                isLooking = false;
                Profile profile = Preferences.getInstance(getContext()).getProfile();
                if (profile != null) {
                    if (profile.getAge() == 0 || profile.getGender() == null || profile.getCity() == null){
                        Toast.makeText(getContext(), R.string.complete_profile, Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getContext(), R.string.connecting, Toast.LENGTH_SHORT).show();
                        String token = Preferences.getInstance(getContext()).getFirebaseToken();
                        D2GOSDK.init(getContext(), token);
                    }
                }
                else Toast.makeText(getContext(), R.string.complete_profile, Toast.LENGTH_SHORT).show();

                D2GOSDK.stopLocationService();
                D2GOSDK.stopBeaconService();
            }

        }

    }


    public static void setStatusColor(int color, int title, int message, boolean alert, int icon){
        if (MainActivityNew.isLogged) {
            if (alert) AlertUtils.sendAlert(activityFragment, activityFragment.getString(title), activityFragment.getString(message), icon);
            ScreenUtils.getInstance().setScreenColor(activityFragment.getWindow(), ContextCompat.getColor(activityFragment.getApplicationContext(), color));
        }
    }

    private void updateBackground() {
        Glide.with(getContext()).load(R.drawable.d2go_welcome).into(background);
    }

    private void validateBluetooth(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(activityFragment, R.string.bluetooth_error, Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enable
                ScreenUtils.getInstance().setScreenColor(activityFragment.getWindow(), ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
            }else {
                // if enabled
                ScreenUtils.getInstance().setScreenColor(activityFragment.getWindow(), ContextCompat.getColor(getContext(), R.color.colorError));
            }

        }
    }
}