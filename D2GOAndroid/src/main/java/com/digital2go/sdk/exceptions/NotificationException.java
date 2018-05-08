package com.digital2go.sdk.exceptions;

/**
 * @autor Digital2Go
 * Created by Ulises Rosas on 18/09/2016.
 */

public class NotificationException extends Exception {

    public NotificationException(String message) {
        super(message);
    }

    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }

}
