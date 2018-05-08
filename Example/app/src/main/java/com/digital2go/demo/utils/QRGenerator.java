package com.digital2go.demo.utils;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

/**
 * Created by Ulises on 13/02/2018.
 */

public class QRGenerator {

    public static Bitmap generateQR(String input, Bundle bundle, int dimension){
        QRGEncoder qrgEncoder = new QRGEncoder(input, bundle, QRGContents.Type.TEXT, dimension);

        Bitmap bitmap = null;

        try {
            // Getting QR-Code as Bitmap
            bitmap = qrgEncoder.encodeAsBitmap();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), e.getMessage()); //if error
        }

        return bitmap;
    }
}
