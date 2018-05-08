package com.digital2go.sdk.exceptions;

/**
 * @author Digital2Go
 * Created by Ulises Rosas on 18/09/2016.
 */
public class LoginException extends Exception {
    public LoginException(String message) {
        super(message);
    }

    public LoginException(String message, Throwable cause) {
        super(message, cause);
    }
}
