package com.digital2go.demo.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;

/**
 * Created by Ulises on 13/02/2018.
 */

public class AlertUtils {

    public static void sendAlert(Activity activity, String title, String message, int icon){
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message)
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                //Set title
                .setTitle(title)
                //Set your icon here
                .setIcon(icon);
        AlertDialog alert = builder.create();
        alert.show();//showing the dialog
    }
}
