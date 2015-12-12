package com.shlmlkzdh.rxsmartlock.exception;

import com.google.android.gms.common.api.Status;


public class StatusException extends RuntimeException {

    private Status mStatus;

    public StatusException(Status status) {
        mStatus = status;
    }

    public Status getStatus() {
        return mStatus;
    }

}
