package com.digital2go.demo;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.digital2go.demo.fragments.FragmentSearch;
import com.digital2go.demo.utils.Preferences;

/**
 * Created by Ulrick on 14/10/2016.
 */
public class CampaignReceiver extends BroadcastReceiver {
    final static String GROUP_KEY_NOTIFY = "d2go_group_key_notify";
    protected NotificationManager mNotificationManager;
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        String campaign = intent.getStringExtra("campaign");
        this.context = context;
        try {
            if (campaign != null) {
                if (campaign != null) {
                    context.sendBroadcast(new Intent("com.d2go.interaction." + String.valueOf(R.string.app_name).toLowerCase()).putExtra("_console", "Campaign arrived"));
                    if (isAppIsInBackground(context)) {
                        sendNotif(campaign, context);
                        Preferences.getInstance(context).addCampaignFound(campaign);
                    } else {
                        if (FragmentSearch.isLooking) {
                            FragmentSearch.newCampaign(campaign);
                            Preferences.getInstance(context).addCampaignFound(campaign);
                            // MainActivityNew.viewPager.setCurrentItem(0, true);
                        }
                    }
                }
            }
        }catch (Exception e){
//            CrashReporter.logException(e);
            e.printStackTrace();
        }

    }

    public void sendNotif(String campaign, Context context){
        if (Preferences.getInstance(context).getProfile().isAlert()){
//            if (!Preferences.getInstance(context).isDisplayed(campaign)){
                new NotifTask(campaign, context).execute();
//            }
        }
    }

    private class NotifTask extends AsyncTask<Void, Void, Bitmap> {

        private JSONObject campaign = null;
        private Context context;

        public NotifTask(String campaign, Context context) {
            try {
                this.campaign = new JSONObject(campaign);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            this.context = context;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap theBitmap = null;
            try {
                JSONObject campaignObject = campaign.getJSONObject("campaign_content");
                try {
                    String imageUrl = campaign.getString("media_cdn");
                    imageUrl += "/" + campaignObject.getJSONObject("media_image").getString("path");
                    imageUrl += campaignObject.getJSONObject("media_image").getString("filename");

                    theBitmap = Glide.with(context).load(imageUrl).asBitmap().into(100, 100).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return theBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            handleNotif(bitmap);
        }

        private void handleNotif(Bitmap image){
            try {
                sendSingleNotification(campaign, image);
                sendStackNotificationsIfNeeded();
            }catch (Exception e){
//                CrashReporter.logException(e);
                e.printStackTrace();
            }
        }

    }

    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    private void sendStackNotificationsIfNeeded(){
        if (Build.VERSION.SDK_INT >= 23){
            ArrayList<StatusBarNotification> groupedNotifications = new ArrayList<>();

            for (StatusBarNotification sbn : getNotificationManagerService().getActiveNotifications()) {
                Log.d(getClass().getSimpleName(), sbn.getNotification().getGroup());
                if (sbn.getNotification().getGroup().equals(GROUP_KEY_NOTIFY)){
                    groupedNotifications.add(sbn);
                }
            }

            if (groupedNotifications.size() >= 0) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                builder.setContentTitle("New Offers!!");

                NotificationCompat.InboxStyle inbox = new NotificationCompat.InboxStyle();
                {
                    for (StatusBarNotification activeSbn : groupedNotifications) {
                        String stackNotificationLine = (String)activeSbn.getNotification().extras.get(NotificationCompat.EXTRA_TITLE);
                        if (stackNotificationLine != null) {
                            inbox.addLine(stackNotificationLine);
                        }
                    }

                    inbox.setSummaryText(String.format("offers %d", groupedNotifications.size()));
                }
                builder.setOnlyAlertOnce(true);
                builder.setAutoCancel(true);
                builder.setStyle(inbox);
                builder.setSmallIcon(getNotificationIcon());

                final int requestCode = (int)System.currentTimeMillis() / 1000;
                builder.setContentIntent(PendingIntent.getActivity(context, requestCode, new Intent(context, MainActivityNew.class), PendingIntent.FLAG_ONE_SHOT));

                builder.setGroup(GROUP_KEY_NOTIFY).setGroupSummary(true);
                builder.setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_HIGH);

                Notification stackNotification = builder.build();
                stackNotification.defaults = Notification.DEFAULT_ALL;

                getNotificationManagerService().notify(GROUP_KEY_NOTIFY, -1000, stackNotification);
            }
        }
    }

    private void sendSingleNotification(JSONObject campaign, Bitmap image){
        Intent result = new Intent(context, MainActivityNew.class);
        try {
            if (campaign != null) {
                result.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                final PendingIntent resultPendingIntent = PendingIntent.getActivity(context, campaign.getInt("id"), result, PendingIntent.FLAG_UPDATE_CURRENT);
                int alarmSound;

                if (Preferences.getInstance(context).getProfile().isSound()){
                    alarmSound = Notification.DEFAULT_SOUND;
                }else alarmSound = 0;

                Notification notification = new NotificationCompat.Builder(context)
                        .setAutoCancel(true)
                        .setOnlyAlertOnce(true)
                        .setContentTitle(campaign.getJSONObject("campaign_content").getString("name"))
                        .setContentText(campaign.getJSONObject("campaign_content").getString("notification_message"))
                        .setContentIntent(resultPendingIntent)
                        .setDefaults(alarmSound)
                        .setSmallIcon(getNotificationIcon())
                        .setLargeIcon(image)
                        .setGroup(GROUP_KEY_NOTIFY)
                        .build();

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(campaign.getInt("id"), notification);

                Preferences.getInstance(context).lastDisplayed(campaign.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected NotificationManager getNotificationManagerService() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.ic_notif : R.mipmap.ic_d2go;
    }
}
