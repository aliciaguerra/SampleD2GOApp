package com.digital2go.sdk.exceptions;

/**
 * @author Digital2Go
 * Created by Ulises Rosas on 18/09/2016.
 */
public class SDKException extends Exception {

    /**
     * Constructor for custom Message
     * @param message Exception Message
     */
    public SDKException(String message) {
        super(message);
    }

    /**
     * Constructor for Custom Message and new Throwable
     *
     * @param message   Exception Message
     * @param throwable New Throwable
     */
    public SDKException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
