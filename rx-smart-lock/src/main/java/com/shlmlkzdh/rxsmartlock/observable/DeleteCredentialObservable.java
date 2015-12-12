package com.shlmlkzdh.rxsmartlock.observable;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.shlmlkzdh.rxsmartlock.exception.ConnectionException;
import com.shlmlkzdh.rxsmartlock.exception.ConnectionSuspendedException;
import com.shlmlkzdh.rxsmartlock.exception.StatusException;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;


public class DeleteCredentialObservable implements Observable.OnSubscribe<Boolean> {

    private Context mContext;
    private Credential mCredential;

    public DeleteCredentialObservable(Context context, Credential credential) {
        mContext = context;
        mCredential = credential;
    }

    @Override
    public void call(Subscriber<? super Boolean> subscriber) {

        final GoogleApiClient googleApiClient = buildGoogleApiClient(subscriber);

        try {
            googleApiClient.connect();
        } catch (Exception e) {
            subscriber.onError(e);
        }

        subscriber.add(Subscriptions.create(new Action0() {
            @Override
            public void call() {
                if (googleApiClient.isConnected() || googleApiClient.isConnecting()) {
                    googleApiClient.disconnect();
                }
            }
        }));

    }

    private GoogleApiClient buildGoogleApiClient(Observer<? super Boolean> observer) {

        GoogleApiClientCallbacks clientCallbacks = new GoogleApiClientCallbacks(observer);
        GoogleApiClient client = new GoogleApiClient.Builder(mContext)
                .addApi(Auth.CREDENTIALS_API)
                .addConnectionCallbacks(clientCallbacks)
                .addOnConnectionFailedListener(clientCallbacks)
                .build();
        clientCallbacks.setGoogleApiClient(client);
        return client;

    }

    private class GoogleApiClientCallbacks implements GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener, ResultCallback {

        private GoogleApiClient mGoogleApiClient;
        private Observer<? super Boolean> mObserver;

        public GoogleApiClientCallbacks(Observer<? super Boolean> observer) {
            mObserver = observer;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onConnected(Bundle bundle) {

            try {
                Auth.CredentialsApi.delete(mGoogleApiClient, mCredential).setResultCallback(this);
            } catch (Exception e) {
                mObserver.onError(e);
            }

        }

        @Override
        public void onConnectionSuspended(int i) {
            mObserver.onError(new ConnectionSuspendedException(i));
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            mObserver.onError(new ConnectionException(connectionResult));
        }

        @Override
        public void onResult(Result result) {

            Status status = result.getStatus();
            if (status.isSuccess()) {
                onCredentialDeleted();
            } else {
                resolveResult(status);
            }

        }

        private void onCredentialDeleted() {
            mObserver.onNext(true);
            mObserver.onCompleted();
        }

        private void resolveResult(Status status) {
            mObserver.onError(new StatusException(status));
        }

        public void setGoogleApiClient(GoogleApiClient googleApiClient) {
            mGoogleApiClient = googleApiClient;
        }

    }

}
