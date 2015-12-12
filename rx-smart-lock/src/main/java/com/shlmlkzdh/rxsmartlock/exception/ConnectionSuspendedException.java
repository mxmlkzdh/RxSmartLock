package com.shlmlkzdh.rxsmartlock.exception;


public class ConnectionSuspendedException extends RuntimeException {

    private int mCode;

    public ConnectionSuspendedException(int code) {
        mCode = code;
    }

    public int getCode() {
        return mCode;
    }

}
