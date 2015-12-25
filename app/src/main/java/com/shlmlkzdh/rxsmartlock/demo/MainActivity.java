package com.shlmlkzdh.rxsmartlock.demo;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.IdentityProviders;
import com.google.android.gms.common.api.Status;
import com.shlmlkzdh.rxsmartlock.SmartLock;
import com.shlmlkzdh.rxsmartlock.exception.StatusException;

import rx.Subscriber;
import rx.functions.Action1;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int CREDENTIAL_REQUEST_RC = 111;
    private SmartLock mSmartLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button_login);
        button.setOnClickListener(this);

        mSmartLock = new SmartLock.Builder(this)
                .setAccountTypes(IdentityProviders.GOOGLE, IdentityProviders.FACEBOOK)
                .setPasswordLoginSupported(true) // Used for password-based sign-in.
                .build();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREDENTIAL_REQUEST_RC) {
            if (resultCode == RESULT_OK) {
                mSmartLock.retrieveCredentialFromIntent(data).subscribe(new Action1<Credential>() {
                    @Override
                    public void call(Credential credential) {
                        onSuccess(credential);
                    }
                });
            } else {
                Log.e(TAG, "Credential Read: NOT OK");
            }
        }

    }

    @Override
    public void onClick(View v) {

        mSmartLock.retrieveCredential().subscribe(new Subscriber<Credential>() {

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

                if (e instanceof StatusException) {
                    Status status = ((StatusException) e).getStatus();
                    if (status.hasResolution()) {
                        try {
                            status.startResolutionForResult(MainActivity.this, CREDENTIAL_REQUEST_RC);
                        } catch (IntentSender.SendIntentException e1) {
                            Log.e(TAG, "STATUS: Failed to send resolution.");
                        }
                    } else {
                        // The user must create an account or sign in manually.
                        Log.e(TAG, "STATUS: Unsuccessful credential request.");
                    }
                }

            }

            @Override
            public void onNext(Credential credential) {
                onSuccess(credential);
            }

        });

    }

    private void onSuccess(Credential credential) {

        Toast.makeText(
                MainActivity.this,
                "You logged in as: " + credential.getName(),
                Toast.LENGTH_SHORT
        ).show();

    }

}
