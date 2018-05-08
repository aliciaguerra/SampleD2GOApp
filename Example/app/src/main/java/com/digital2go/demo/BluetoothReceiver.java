package com.digital2go.demo;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.digital2go.demo.fragments.FragmentSearch;

/**
 * Created by pc on 27/09/2017.
 */

public class BluetoothReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    FragmentSearch.setStatusColor(R.color.colorError, R.string.bt_alert_title, R.string.bt_alert_message, true, R.mipmap.ic_bluetooth);
                    context.sendBroadcast(new Intent("com.d2go.interaction."+String.valueOf(R.string.app_name).toLowerCase()).putExtra("_console", "Bluetooth Disabled"));
                    break;

                case BluetoothAdapter.STATE_ON:
                    FragmentSearch.setStatusColor(R.color.pure_black, 0, 0, false, 0);
                    context.sendBroadcast(new Intent("com.d2go.interaction"+String.valueOf(R.string.app_name).toLowerCase()).putExtra("_console", "Bluetooth Enabled"));
                    break;
            }
        }
    }
}
