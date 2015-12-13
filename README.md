# RxSmartLock
Reactive extension for Google's [Smart Lock for Passwords API][1].

## Introduction
[Sebastiano Poggi][5] has written a fantastic blog post on Smart Lock. Read it [here][6].

## Usage

In order to use **RxSmartLock**, build a `SmartLock` object using the `SmartLock.Builder` class in the `onCreate` method of your `Activity`:

```java
public class MainActivity extends AppCompatActivity {

    private SmartLock mSmartLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSmartLock = new SmartLock.Builder(this)
                .setAccountTypes(IdentityProviders.GOOGLE, IdentityProviders.FACEBOOK) // Use the constants defined in IdentityProviders to specify commonly-used sign-in providers.
                .setPasswordLoginSupported(true) // Used for password-based sign-in.
                .build();

    }

}
```
Now you are ready to use **RxSmartLock**.

### Retrieve a User's Stored Credentials

Automatically sign users into your app by using the Credentials API to request and retrieve stored credentials for your users.

To request stored credentials, call the `retrieveCredential()` method on the `SmartLock` object that you created from anywhere in your `Activity`. This method returns an observabale `Observable<Credential>` which you can subscribe to:
```java
mSubscription = mSmartLock.retrieveCredential().subscribe(new Subscriber<Credential>() {

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {

        // Handle unsuccessful and incomplete credential requests.
        if (e instanceof StatusException) {
            Status status = ((StatusException) e).getStatus();
            if (status.hasResolution()) {
                // Prompt the user to choose a saved credential; do not show the hint selector.
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
        // On a successful credential request, use the resulting Credential object to complete the user's sign-in to your app.
    }

});
```
When user input is required to select a credential, the `getStatusCode()` method returns `RESOLUTION_REQUIRED`. In this case, call the status object's `startResolutionForResult()` method to prompt the user to choose an account. Then, retrieve the user's chosen credentials from the activity's `onActivityResult()` method by calling the `retrieveCredentialFromIntent(Intent)` method on the `SmartLock` object that you created. This method returns an observabale `Observable<Credential>` which you can again subscribe to:

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
    super.onActivityResult(requestCode, resultCode, data);
        
    if (requestCode == CREDENTIAL_REQUEST_RC) {
        if (resultCode == RESULT_OK) {
            mSmartLock.retrieveCredentialFromIntent(data).subscribe(new Action1<Credential>() {
                @Override
                public void call(Credential credential) {
                    // On a successful credential request, use the resulting Credential object to complete the user's sign-in to your app.
                }
            });
        } else {
            Log.e(TAG, "Credential Read: NOT OK");
        }
    }

}
```

For more information, check out the [official documentaion][2] on how to handle successful credential requests.

### Store a User's Credentials

After users successfully sign in, create accounts, or change passwords, allow them to store their credentials to automate future authentication in your app.

To save users' credentials, you can call the `storeCredential(Credential)` method on the `SmartLock` object that you created from anywhere in your `Activity`. This method returns an observabale `Observable<Boolean>` which you can subscribe to:
```java
mSubscription = mSmartLock.storeCredential(credential).subscribe(new Subscriber<Boolean>() {
            
    @Override
    public void onCompleted() {
      
    }

    @Override
    public void onError(Throwable e) {
    
        // Try to resolve the save request. 
        // This will prompt the user if the credential is new.
        if (e instanceof StatusException) {
            Status status = ((StatusException) e).getStatus();
            if (status.hasResolution()) {
                try {
                    status.startResolutionForResult(MainActivity.this, CREDENTIAL_STORE_RC);
                } catch (IntentSender.SendIntentException e1) {
                    Log.e(TAG, "STATUS: Failed to send resolution.");
                }
            }
        }
        
    }

    @Override
    public void onNext(Boolean aBoolean) {
    
        if (aBoolean) {
            // We have successfully saved the user's credential.
        }
        
    }
            
});
```

If the call to `SmartLock.storeCredential(Credential)` is not immediately successful, the credentials might be new, in which case the user must confirm the save request. Resolve the save request with `startResolutionForResult()` to prompt the user for confirmation.

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == CREDENTIAL_STORE_RC) {
        if (resultCode == RESULT_OK) {
            Log.d(TAG, "SAVE: OK");
        } else {
            Log.e(TAG, "SAVE: Canceled by user.");
        }
    }

}
```

To learn more about how to store a user's credentials, check out the [official documentaion][3].

### Delete Stored Credentials

Delete credentials from Smart Lock when either of the following circumstances occur:

- Signing in with the credentials fails because the account no longer exists or the password is incorrect.
- The user completes the app's account deletion flow.

To delete credentials, you can call the `deleteCredential(Credential)` method on the `SmartLock` object that you created from anywhere in your `Activity`. This method returns an observabale `Observable<Boolean>` which you can subscribe to:
```java
mSubscription = mSmartLock.deleteCredential(credential).subscribe(new Subscriber<Boolean>() {
            
    @Override
    public void onCompleted() {
      
    }

    @Override
    public void onError(Throwable e) {
    
        // Handle unsuccessful deletion.
        if (e instanceof StatusException) {
            Status status = ((StatusException) e).getStatus();
            Log.e(TAG, status.getStatusMessage());
        }
        
    }

    @Override
    public void onNext(Boolean aBoolean) {
    
        if (aBoolean) {
            // Credential was deleted successfully.
        }
        
    }
            
});
```

### Error Handling

Since [Smart Lock for Passwords][1] is a Google Play Services API, we need to handle possible connection failures when the connection to Google Play services fails or becomes suspended. This is how you can deal with these situtations gracefully when using **RxSmartLock**:

```java
@Override
public void onError(Throwable e) {

    if (e instanceof ConnectionSuspendedException) {
        int errorCode = ((ConnectionSuspendedException) e).getCode();
        // Handle the connection suspension errors here.
    }

}
```

```java
@Override
public void onError(Throwable e) {

    if (e instanceof ConnectionException) {
        ConnectionResult result = ((ConnectionException) e).getConnectionResult();
        // Handle the connection failed errors here.
        if (result.hasResolution()) {
            result.startResolutionForResult(MainActivity.this, RESULT_RC);
        }
    }

}
```

You can find more information on how to handle connection failures [here][4].

## Add RxSmartLock To Your Project

Add this to your **build.gradle** file:
```java
repositories {
    maven { url "https://jitpack.io" }
}
dependencies {
  compile 'com.github.ShlMlkzdh:RxSmartLock:1.0.2'
}
```

## Additional Notes

This library is inspired by the [ReactiveLocation library for Android][7] developed by [Michał Charmas][8] and a great [blog post][6] written by [Sebastiano Poggi][5] on Smart Lock. Thank you guys!

 [1]: https://developers.google.com/identity/smartlock-passwords/android/
 [2]: https://developers.google.com/identity/smartlock-passwords/android/retrieve-credentials
 [3]: https://developers.google.com/identity/smartlock-passwords/android/store-credentials
 [4]: https://developers.google.com/android/guides/api-client
 [5]: https://medium.com/@seebrock3r
 [6]: https://medium.com/@seebrock3r/login-experiences-that-don-t-suck-a42fade07067
 [7]: https://github.com/mcharmas/Android-ReactiveLocation
 [8]: https://github.com/mcharmas
