package com.apriorit.android.processmonitoring;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends Activity {
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private String mToken;
    private String mSenderId;
    private EditText mEditTextToken;
    private EditText mEditTextMessage;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditTextToken = (EditText)findViewById(R.id.editTextToken);
        mEditTextMessage = (EditText)findViewById(R.id.editTextMessage);

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
        mSenderId = getString(R.string.gcm_defaultSenderId);
        Log.d("SenderID", mSenderId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_SUCCESS));

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_ERROR));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    /**
     * Sends message to GCM server
     * GCM server will deliver it to our XMPP server
     */
    public void SendMessageToServer(final View view) {
        final Bundle data = new Bundle();
        data.putString("message", mEditTextMessage.getText().toString());
        if (view == findViewById(R.id.btnSend)) {
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    try {
                        AtomicInteger msgId = new AtomicInteger();
                        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                        gcm.send(mSenderId + "@gcm.googleapis.com", msgId.toString(), data);
                        return null;
                    } catch (IOException ex) {
                        return "Error sending upstream message:" + ex.getMessage();
                    }
                }
                @Override
                protected void onPostExecute(String result) {
                    if (result != null) {
                        Toast.makeText(getApplicationContext(),
                                "send message failed: " + result,
                                Toast.LENGTH_LONG).show();
                    }
                }
            }.execute(null, null, null);
        }
    }
    public void HideApplication(View v) {
        //hide an application icon from Android applications list
        PackageManager pm = getApplicationContext().getPackageManager();
        pm.setComponentEnabledSetting(getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
