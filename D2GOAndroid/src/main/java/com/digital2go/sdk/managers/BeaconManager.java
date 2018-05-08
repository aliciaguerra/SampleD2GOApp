package com.digital2go.sdk.managers;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.digital2go.sdk.D2GOSDK;
import com.digital2go.sdk.exceptions.SDKException;
import com.digital2go.sdk.services.BeaconService;

/**
 * @author Digital2Go
 * Created by Ulises Rosas on 24/08/2016.
 */
public class BeaconManager {
    private static BeaconManager instance = null;

    /**
     * Singleton
     * @return BeaconManager instance
     */
    public static synchronized BeaconManager getInstance(){
        if(instance == null) instance = new BeaconManager();
        return instance;
    }

    /** start beacon service */
    public void startService(final Context context) throws SDKException {
        if (context != null) {
            boolean hasBluetooth = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
            boolean hasBluetoothLe = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);

            if (hasBluetooth && hasBluetoothLe){
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter != null) context.startService(new Intent(context, BeaconService.class));
            }
        }
    }

    /** stop beacon service */
    public void stopService(final Context context){
        if (context != null) context.stopService(new Intent(context, BeaconService.class));
    }
}
