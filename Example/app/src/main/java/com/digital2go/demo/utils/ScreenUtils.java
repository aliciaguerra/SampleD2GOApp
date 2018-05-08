package com.digital2go.demo.utils;

import android.content.Context;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Singleton util for Window Updates
 * Created by Ulrick on 27/09/2017.
 */

public class ScreenUtils {

    private static ScreenUtils instance;

    private ScreenUtils() {

    }

    public static ScreenUtils getInstance(){
        if (instance == null) instance = new ScreenUtils();

        return instance;
    }

    public void setScreenColor(Window window, int color){
        /** Changes status bar color **/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    public void copyClipboard(Context context, String text) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
        Toast.makeText(context, "Copied to Clipboard", Toast.LENGTH_SHORT).show();

    }

}
