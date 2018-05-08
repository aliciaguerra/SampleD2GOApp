package com.digital2go.sdk.receivers;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.digital2go.sdk.managers.BeaconManager;
import com.digital2go.sdk.utils.SDKPreferences;


/**
 * @author Digital2GO
 * Created by Ulises Rosas on 8/2/16.
 */
public class BluetoothReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SDKPreferences preferences = SDKPreferences.getInstance();
        final String action = intent.getAction();

        if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR);

            try {
                switch (state){
                    case BluetoothAdapter.STATE_ON:
                        if(preferences.getBeaconsStatus(context.getApplicationContext())) BeaconManager.getInstance().startService(context.getApplicationContext());
                        else BeaconManager.getInstance().stopService(context.getApplicationContext());
                        break;

                    case BluetoothAdapter.STATE_TURNING_OFF:
                        BeaconManager.getInstance().stopService(context.getApplicationContext());
                        break;
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
