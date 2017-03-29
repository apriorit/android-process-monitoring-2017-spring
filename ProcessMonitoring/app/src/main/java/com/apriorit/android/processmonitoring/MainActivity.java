package com.apriorit.android.processmonitoring;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.apriorit.android.processmonitoring.request_handler.Handler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends Activity {
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private String mToken;
    private EditText mEditTextToken;
    private EditText mEditTextBlacklist;
    private Handler requestHander;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEditTextToken = (EditText)findViewById(R.id.editTextToken);
        mEditTextBlacklist = (EditText)findViewById(R.id.editTextBlacklist);

        gcmRegistration();
        requestHander = new Handler(this);
        registerAccount();
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
                if(intent.getAction().endsWith(GCMRegistrationIntentService.REGISTRATION_SUCCESS)) {
                    //Registration success
                    mToken = intent.getStringExtra("token");
                    Toast.makeText(getApplicationContext(), "GCM token:" + mToken, Toast.LENGTH_LONG).show();
                    mEditTextToken.setText(mToken);
                } else if(intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_ERROR)) {
                    //Registration error
                    Toast.makeText(getApplicationContext(), "GCM registration error!!!", Toast.LENGTH_LONG).show();
                }
            }
        };
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        //Check status of Google play service in device
        int resultCode = googleAPI.isGooglePlayServicesAvailable(getApplicationContext());
        if(ConnectionResult.SUCCESS != resultCode) {
            //Check type of error
            if(googleAPI.isUserResolvableError(resultCode)) {
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
    /**
     * Sends some registration data to GCM server
     * XMPP server saves in database user data and device token
     */
    private void registerAccount() {
        Bundle registrationData = new Bundle();
        registrationData.putString("login", "User");
        registrationData.putString("password", "some password");
        requestHander.SendDataToServer(registrationData);
    }
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_SUCCESS));

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_ERROR));

        //Registrate receiver which gets list with apps
        registerReceiver(broadcastReceiver, new IntentFilter("BLACKLIST"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
    /**
     * Sends message to GCM server
     * GCM server will deliver it to our XMPP server
     */
    public void SendListAppsToServer(final View view) {
        requestHander.HandleListApps();
    }
    public void HideApplication(View v) {
        //hide an application icon from Android applications list
        PackageManager pm = getApplicationContext().getPackageManager();
        pm.setComponentEnabledSetting(getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }
    //Receives list with app in order to display in activity
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(intent);
        }
    };
    //Displays list in view component
    private void updateUI(Intent intent) {
        mEditTextBlacklist.setText(intent.getStringExtra("list"));
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}

