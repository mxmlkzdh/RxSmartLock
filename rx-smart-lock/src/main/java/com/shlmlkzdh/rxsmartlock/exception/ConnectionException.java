package com.shlmlkzdh.rxsmartlock.exception;

import com.google.android.gms.common.ConnectionResult;


public class ConnectionException extends RuntimeException {

    private ConnectionResult mConnectionResult;

    public ConnectionException(ConnectionResult result) {
        mConnectionResult = result;
    }

    public ConnectionResult getConnectionResult() {
        return mConnectionResult;
    }

}
