package com.digital2go.demo.utils;

import android.content.Context;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.digital2go.demo.models.Profile;

import static com.digital2go.demo.utils.Constants.APP_FILE;
import static com.digital2go.demo.utils.Constants.CAMPAIGNS;
import static com.digital2go.demo.utils.Constants.CAMPAIGN_FOUND;
import static com.digital2go.demo.utils.Constants.FIREBASE_TOKEN;
import static com.digital2go.demo.utils.Constants.LAST_CAMPAIGN;
import static com.digital2go.demo.utils.Constants.LOGIN;
import static com.digital2go.demo.utils.Constants.PROFILE;

/**
 * Created by Ulrick on 10/10/2016.
 */
public class Preferences {

    private Context mContext;
    private static Preferences instance;

    private Preferences(Context mContext) {
        this.mContext = mContext;
    }

    public static Preferences getInstance(Context mContext){
        if (instance == null) instance = new Preferences(mContext);

        return instance;
    }

    /**
     * Gets UserProfile stored in sharedPreferences
     * @return Profile with the user data
     */
    public Profile getProfile(){
        String profile = mContext.getSharedPreferences(APP_FILE, Context.MODE_PRIVATE).getString(PROFILE, "");
        if(profile == null || profile.isEmpty()) return null;
        else return new Gson().fromJson(profile, Profile.class);
    }

    /**
     * Stores UserProfile to sharedPreferences
     * @param profile UserProfile
     */
    public void saveProfile(Profile profile){
        mContext.getSharedPreferences(APP_FILE, Context.MODE_PRIVATE)
                .edit()
                .putString(PROFILE, new Gson().toJson(profile))
                .apply();
    }

    /**
     * Stores list of campaigns in sharedPreferences
     * @param campaigns Campaigns
     */
    public void saveCampaigns(JSONArray campaigns){
        mContext.getSharedPreferences(APP_FILE, Context.MODE_PRIVATE)
                .edit()
                .putString(CAMPAIGNS, campaigns.toString())
                .apply();
    }

    /**
     * Gets the list of campaigns stored in sharedPreferences
     * @return List of Campaigns
     */
    public JSONArray getCampaigns(){
        String sCampaigns = mContext.getSharedPreferences(APP_FILE, Context.MODE_PRIVATE).getString(CAMPAIGNS, null);
        JSONArray campaigns = new JSONArray();
        try {
            if (sCampaigns != null) {
                return new JSONArray(sCampaigns);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return campaigns;
    }

    /**
     * Sets Login Status
     * @param value True if logged, false otherwise
     */
    public void setLogged(boolean value){
        mContext.getSharedPreferences(APP_FILE, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(LOGIN, value)
                .apply();
    }

    /**
     * Gets Login Status
     * @return True if logged, false otherwise
     */
    public boolean isLogged(){
        return mContext.getSharedPreferences(APP_FILE, Context.MODE_PRIVATE)
                .getBoolean(LOGIN, false);
    }

    public boolean isDisplayed(String campaign){
        boolean displayed = false;
        String strCampaign = mContext.getSharedPreferences(APP_FILE, Context.MODE_PRIVATE).getString(LAST_CAMPAIGN, null);

        if (strCampaign != null) {
            try {
                JSONObject campaignObject = new JSONObject(campaign);
                JSONObject lastCampaign = new JSONObject(strCampaign);
                if (lastCampaign.getInt("id") == campaignObject.getInt("id")) {
                    displayed = true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return displayed;
    }

    public void lastDisplayed(String campaign){
        mContext.getSharedPreferences(APP_FILE, Context.MODE_PRIVATE)
                .edit()
                .putString(LAST_CAMPAIGN, campaign)
                .apply();
    }

    public void addCampaignFound(String campaign){
        JSONArray campaigns = getCampaignsFound();
        try {
            if (!containsCampaign(campaign, campaigns)) campaigns.put(new JSONObject(campaign));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mContext.getSharedPreferences(APP_FILE, Context.MODE_PRIVATE)
                .edit()
                .putString(CAMPAIGN_FOUND, campaigns.toString())
                .apply();
    }

    public JSONArray getCampaignsFound(){
        String sCampaigns = mContext.getSharedPreferences(APP_FILE, Context.MODE_PRIVATE).getString(CAMPAIGN_FOUND, null);

        JSONArray campaigns = new JSONArray();
        try {
            if (sCampaigns != null) {
                return new JSONArray(sCampaigns);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return campaigns;
    }

    public void clearCampaignsFound(){
        mContext.getSharedPreferences(APP_FILE, Context.MODE_PRIVATE)
                .edit()
                .putString(CAMPAIGN_FOUND, null)
                .apply();
    }

    public String getFirebaseToken(){
        return mContext.getSharedPreferences(APP_FILE, Context.MODE_PRIVATE)
                .getString(FIREBASE_TOKEN, null);
    }

    public void storeFirebaseToken(String token){
        mContext.getSharedPreferences(APP_FILE, Context.MODE_PRIVATE)
                .edit()
                .putString(FIREBASE_TOKEN, token)
                .apply();
    }

    private boolean containsCampaign(String campaign, JSONArray campaigns) throws JSONException {
        boolean result = false;
        JSONObject object = new JSONObject(campaign);

        for ( int i = 0; i < campaigns.length(); i++){
            if (campaigns.getJSONObject(i).getInt("id") == object.getInt("id")) {
                result = true;
                break;
            }
        }
        return  result;
    }
}
