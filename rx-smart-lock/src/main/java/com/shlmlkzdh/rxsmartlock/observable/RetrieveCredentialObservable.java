package com.shlmlkzdh.rxsmartlock.observable;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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


public class RetrieveCredentialObservable implements Observable.OnSubscribe<Credential> {

    private Context mContext;
    private CredentialRequest mCredentialRequest;

    public RetrieveCredentialObservable(Context context, CredentialRequest request) {
        mContext = context;
        mCredentialRequest = request;
    }

    @Override
    public void call(Subscriber<? super Credential> subscriber) {

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

    private GoogleApiClient buildGoogleApiClient(Observer<? super Credential> observer) {

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
            GoogleApiClient.OnConnectionFailedListener, ResultCallback<CredentialRequestResult> {

        private GoogleApiClient mGoogleApiClient;
        private Observer<? super Credential> mObserver;

        public GoogleApiClientCallbacks(Observer<? super Credential> observer) {
            mObserver = observer;
        }

        @Override
        public void onConnected(Bundle bundle) {

            try {
                Auth.CredentialsApi.request(mGoogleApiClient, mCredentialRequest)
                        .setResultCallback(this);
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
        public void onResult(CredentialRequestResult credentialRequestResult) {

            Status status = credentialRequestResult.getStatus();
            if (status.isSuccess()) {
                onCredentialRetrieved(credentialRequestResult.getCredential());
            } else {
                resolveResult(status);
            }

        }

        private void onCredentialRetrieved(Credential credential) {
            mObserver.onNext(credential);
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
