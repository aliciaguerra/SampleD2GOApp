package com.digital2go.sdk.utils;

import android.content.Context;
import android.os.BatteryManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import static android.content.Context.BATTERY_SERVICE;

/**
 * @author Digital2Go
 * Created by Ulises Rosas on 23/11/2017.
 */

public class DeviceUtils {

    public static void info(final Context context) throws JSONException {
        JSONObject deviceInfo = new JSONObject();
        deviceInfo.put("ip_address6", getLocalIpV6());
        deviceInfo.put("ip_address4", getIpv4());
        deviceInfo.put("battery_level", getBatteryLevel(context));
        deviceInfo.put("device_model", getDeviceModel());
        deviceInfo.put("device_os", getDeviceOs());
        deviceInfo.put("device_version", getDevice_version());
        deviceInfo.put("speed", -1);
        SDKPreferences.getInstance().storeDeviceInfo(deviceInfo.toString(), context);
    }

    private static String getLocalIpV6() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();

                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet6Address) {
                        String ipaddress = inetAddress.getHostAddress().toString().split("%")[0];
                        return ipaddress;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static String getIpv4() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        String ipaddress = inetAddress.getHostAddress().toString();
                        return ipaddress;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static String getBatteryLevel(Context context){
        BatteryManager bm = (BatteryManager)context.getSystemService(BATTERY_SERVICE);
        int batLevel = 0;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        }

        return String.valueOf(batLevel);
    }

    private static String getDeviceModel() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String getDeviceOs(){
        StringBuilder builder = new StringBuilder();
        builder.append("android: ").append(Build.VERSION.RELEASE);

        Field[] fields = Build.VERSION_CODES.class.getFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            int fieldValue = -1;

            try {
                fieldValue = field.getInt(new Object());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            if (fieldValue == Build.VERSION.SDK_INT) {
                builder.append(" : ").append(fieldName);
            }
        }

        return builder.toString();
    }

    private static String getDevice_version() {
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        return String.valueOf(SDK_INT);
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }
}
