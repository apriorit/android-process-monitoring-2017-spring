package com.apriorit.android.processmonitoring.registration;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.apriorit.android.processmonitoring.R;
import com.apriorit.android.processmonitoring.request_handler.Handler;

public class RegistrationActivity extends AppCompatActivity {
    private Handler mRequestHandler;
    private TextView mLogin;
    private TextView mPassword;
    private TextView mKey;
    private TextView mKeyVerify;
    private SharedPreferencesHandler mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mLogin = (TextView) findViewById(R.id.login);
        mPassword = (TextView) findViewById(R.id.password);
        mKey = (TextView) findViewById(R.id.master_key);
        mKeyVerify = (TextView) findViewById(R.id.master_key_verify);
        mRequestHandler = new Handler(this);

        mSharedPref = new SharedPreferencesHandler(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter("registration"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    /**
     * Sends some registration data to GCM server
     * XMPP server saves in database user data and device token
     */
    public void registerAccount(View v) {
        if (mKey.getText().toString().equals(mKeyVerify.getText().toString())) {
            Bundle registrationData = new Bundle();
            registrationData.putString("requestType", "account_registration");
            registrationData.putString("login", mLogin.getText().toString());
            registrationData.putString("password", mPassword.getText().toString());
            registrationData.putString("key", mKey.getText().toString());
            mRequestHandler.SendDataToServer(registrationData);
        } else {
            Toast.makeText(getApplicationContext(), "Your key and confirmation key don't match!", Toast.LENGTH_LONG).show();
        }
    }

    public void openAuthenticationActivity(View v) {
        Intent intent = new Intent(RegistrationActivity.this, AuthenticationActivity.class);
        RegistrationActivity.this.startActivity(intent);
    }

    //Receives status of registration
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("status").equals("success")) {
                //save login and master key
                mSharedPref.saveLogin(mLogin.getText().toString());
                mSharedPref.saveKey(mKey.getText().toString());

                Intent intentSelectMode = new Intent(RegistrationActivity.this, SelectModeActivity.class);
                intentSelectMode.putExtra("login", mLogin.getText().toString());
                RegistrationActivity.this.startActivity(intentSelectMode);
            } else {
                Toast.makeText(getApplicationContext(), "Login is already taken", Toast.LENGTH_LONG).show();
            }
        }
    };
}
