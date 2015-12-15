package com.shlmlkzdh.rxsmartlock;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialPickerConfig;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.shlmlkzdh.rxsmartlock.observable.DeleteCredentialObservable;
import com.shlmlkzdh.rxsmartlock.observable.RetrieveCredentialObservable;
import com.shlmlkzdh.rxsmartlock.observable.StoreCredentialObservable;

import rx.Observable;
import rx.functions.Func0;


public class SmartLock {

    private Context mContext;
    private CredentialRequest mCredentialRequest;

    private SmartLock(Builder builder) {
        mContext = builder.getContext();
        mCredentialRequest = builder.getCredentialRequestBuilder().build();
    }

    public Observable<Credential> retrieveCredential() {

        return Observable.defer(new Func0<Observable<Credential>>() {
            @Override
            public Observable<Credential> call() {
                try {
                    return Observable.create(
                            new RetrieveCredentialObservable(mContext, mCredentialRequest)
                    );
                } catch (Exception e) {
                    return Observable.error(e);
                }
            }
        });

    }

    public Observable<Credential> retrieveCredentialFromIntent(Intent data) {
        return Observable.just((Credential) data.getParcelableExtra(Credential.EXTRA_KEY));
    }

    public Observable<Boolean> storeCredential(final Credential credential) {

        return Observable.defer(new Func0<Observable<Boolean>>() {
            @Override
            public Observable<Boolean> call() {
                try {
                    return Observable.create(
                            new StoreCredentialObservable(mContext, credential)
                    );
                } catch (Exception e) {
                    return Observable.error(e);
                }
            }
        });

    }

    public Observable<Boolean> deleteCredential(final Credential credential) {

        return Observable.defer(new Func0<Observable<Boolean>>() {
            @Override
            public Observable<Boolean> call() {
                try {
                    return Observable.create(
                            new DeleteCredentialObservable(mContext, credential)
                    );
                } catch (Exception e) {
                    return Observable.error(e);
                }
            }
        });

    }

    public static class Builder {

        private Context mContext;
        private CredentialRequest.Builder mCredentialRequestBuilder;

        public Builder(Context context) {
            mContext = context;
            mCredentialRequestBuilder = new CredentialRequest.Builder();
        }

        public Builder setAccountTypes(String... accountTypes) {
            mCredentialRequestBuilder.setAccountTypes(accountTypes);
            return this;
        }

        public Builder setCredentialHintPickerConfig(CredentialPickerConfig config) {
            mCredentialRequestBuilder.setCredentialHintPickerConfig(config);
            return this;
        }

        public Builder setCredentialPickerConfig(CredentialPickerConfig config) {
            mCredentialRequestBuilder.setCredentialPickerConfig(config);
            return this;
        }

        public Builder setPasswordLoginSupported(boolean passwordLoginSupported) {
            mCredentialRequestBuilder.setPasswordLoginSupported(passwordLoginSupported);
            return this;
        }

        public SmartLock build() {
            return new SmartLock(this);
        }

        private Context getContext() {
            return mContext;
        }

        private CredentialRequest.Builder getCredentialRequestBuilder() {
            return mCredentialRequestBuilder;
        }

    }

}
