package com.apriorit.android.processmonitoring.registration;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.apriorit.android.processmonitoring.GCMRegistrationIntentService;
import com.apriorit.android.processmonitoring.R;
import com.apriorit.android.processmonitoring.request_handler.Handler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class AuthenticationActivity extends AppCompatActivity {
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private Handler mRequestHandler;
    private TextView mLogin;
    private TextView mPassword;
    private SharedPreferencesHandler mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        gcmRegistration();

        mLogin = (TextView) findViewById(R.id.authentication_login);
        mPassword = (TextView) findViewById(R.id.authentication_password);

        mRequestHandler = new Handler(this);
        mSharedPref = new SharedPreferencesHandler(this);

        String accessibilityState = mSharedPref.getAccessibiltiyState();
        String login = mSharedPref.getLogin();
        if (accessibilityState != null && accessibilityState.equals("enabled")) {
            //User has to finish configuration
            Intent intentSettings = new Intent(AuthenticationActivity.this, SetSettingsActivity.class);
            startActivity(intentSettings);
        } else {
            //If we have saved login in shared preferences - open SelectModeActivity
            if (login != null) {
                openSelectModeActivity(login);
            }
        }
    }

    //Server will check if user has entered correct login and password
    public void authenticationWithServer(View v) {
        mRequestHandler.userAuthentication(mLogin.getText().toString(), mPassword.getText().toString());
    }

    //Receives status
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra("status");
            if (data.equals("failed")) {
                Toast.makeText(getApplicationContext(), "Incorrect username or password", Toast.LENGTH_LONG).show();
            } else {
                //save in shared preferences
                mSharedPref.saveLogin(mLogin.getText().toString());
                mSharedPref.saveKey(data);
                //open next activity to select mode
                openSelectModeActivity(mLogin.getText().toString());
            }
        }
    };

    /**
     * opens activity where user will chose whether control this device or others
     */
    private void openSelectModeActivity(String login) {
        Intent intentSelectMode = new Intent(AuthenticationActivity.this, SelectModeActivity.class);
        intentSelectMode.putExtra("login", login);
        AuthenticationActivity.this.startActivity(intentSelectMode);
    }


    public void openRegistrationActivity(View v) {
        Intent intent = new Intent(AuthenticationActivity.this, RegistrationActivity.class);
        AuthenticationActivity.this.startActivity(intent);
    }

    /*
      Registration device in GCM
      Obtains new device token
     */
    private void gcmRegistration() {
        //This is the handler that will manager to process the broadcast intent
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //check type of intent filter
                if (intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_ERROR)) {
                    //Registration error
                    Toast.makeText(getApplicationContext(), "GCM registration error!!!", Toast.LENGTH_LONG).show();
                }
            }
        };
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        //Check status of Google play service in device
        int resultCode = googleAPI.isGooglePlayServicesAvailable(getApplicationContext());
        if (ConnectionResult.SUCCESS != resultCode) {
            //Check type of error
            if (googleAPI.isUserResolvableError(resultCode)) {
                Toast.makeText(getApplicationContext(), "Google Play Service is not install/enable in this device!", Toast.LENGTH_LONG).show();
                googleAPI.showErrorNotification(getApplicationContext(), resultCode);
            } else {
                Toast.makeText(getApplicationContext(), "This device does not support for Google Play Service!", Toast.LENGTH_LONG).show();
            }
        } else {
            //Start service
            Intent intent = new Intent(this, GCMRegistrationIntentService.class);
            startService(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter("authentication"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_SUCCESS));

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_ERROR));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
}
