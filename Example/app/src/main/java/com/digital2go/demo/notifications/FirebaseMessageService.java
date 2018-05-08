package com.digital2go.demo.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.digital2go.demo.MainActivity;
import com.digital2go.demo.MainActivityNew;
import com.digital2go.demo.R;
import com.digital2go.demo.utils.Preferences;

/**
 * Created by Ulises on 26/12/2017.
 */

public class FirebaseMessageService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

            //Check if the message contains data
            if(remoteMessage.getData().size() > 0) {
                Log.d(TAG, "Message data: " + remoteMessage.getData());
                sendNotification(remoteMessage.getData().get("default"));
            }

            //Check if the message contains notification
            if(remoteMessage.getNotification() != null) {
                Log.d(TAG, "Mesage body:" + remoteMessage.getNotification().getBody());
                sendNotification(remoteMessage.getNotification().getBody());
            }

            Log.i(TAG, remoteMessage.toString());
    }

    /**
     * Dispay the notification
     * @param body
     */
    private void sendNotification(String body) {

        Intent intent = new Intent(this, MainActivityNew.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0/*Request code*/, intent, PendingIntent.FLAG_ONE_SHOT);
        //Set sound of notification
        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notifiBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(getNotificationIcon())
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(notificationSound)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /*ID of notification*/, notifiBuilder.build());
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.mipmap.ic_notif : R.mipmap.ic_d2go;
    }
}

