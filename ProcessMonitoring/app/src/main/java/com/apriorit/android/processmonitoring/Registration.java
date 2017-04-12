package com.apriorit.android.processmonitoring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.apriorit.android.processmonitoring.request_handler.Handler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class Registration extends AppCompatActivity {

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private String mToken;
    private Handler requestHander;
    public static final String APP_PREFERENCES = "preference";
    public static final String APP_PREFERENCES_KEY = "masterKey";
    public static final String APP_PREFERENCES_ACCOUNT_NAME = "accountName";
    public static final String APP_PREFERENCES_TOKEN_ID = "tokenId";
    public static final int MIN_KEY_LENGHT = 4;
    public static final int MIN_LOGIN_LENGHT = 6;
    public static final int MIN_PASS_LENGHT = 8;
    SharedPreferences masterKey;
    SharedPreferences accountName;
    SharedPreferences tokenId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestHander = new Handler(this);
        //initialize variable for save master key,accoun name, tokenId in phone memory
        masterKey = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        accountName = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        tokenId = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        setContentView(R.layout.activity_registration);


        //gcmRegistration();

        final TextView key = (TextView) findViewById(R.id.master_key);
        final TextView keyVetify = (TextView) findViewById(R.id.master_key_verify);
        final TextView login = (TextView) findViewById(R.id.login);
        final TextView pass = (TextView) findViewById(R.id.password);
        Button btnRegister = (Button) findViewById(R.id.btnRegister);
        Button btnToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String loginText = login.getText().toString();
                String passText = pass.getText().toString();


                //start save master key
                String keyText = key.getText().toString();
                String keyTextVerify = keyVetify.getText().toString();
//                if((key.length() >=  MIN_KEY_LENGHT ) && (!key.equals(""))  && (keyTextVerify.equals(keyText))
//                && login.length() >=  MIN_LOGIN_LENGHT && login.length() >= MIN_PASS_LENGHT) {
//                    ?save masterKey
                    SharedPreferences.Editor e = masterKey.edit();
                    e.putString(APP_PREFERENCES_KEY, keyTextVerify);
                    e.commit();
                    //Save account Name
                    SharedPreferences.Editor accName = accountName.edit();
                    accName.putString(APP_PREFERENCES_ACCOUNT_NAME, loginText);
                    accName.commit();
                    gcmRegistration();
//                    registerAccount( loginText, passText,keyText);
                    Intent intent = new Intent(Registration.this, Select_user.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
//                }

                //end save master key

            }
        });

        btnToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Registration.this, Login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

    }


    protected void gcmRegistration() {

        Log.d("MY", "gcaRegistration work");
        //This is the handler that will manager to process the broadcast intent
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //check type of intent filter
                if (intent.getAction().endsWith(GCMRegistrationIntentService.REGISTRATION_SUCCESS)) {
                    //Registration success
                    mToken = intent.getStringExtra("token");
                    Toast.makeText(getApplicationContext(), "GCM token:" + mToken, Toast.LENGTH_LONG).show();
                    Log.d("MY", mToken);

//                    SharedPreferences.Editor tId = tokenId.edit();
//                    tId.putString(APP_PREFERENCES_TOKEN_ID, mToken);
//                    tId.commit();

                } else if (intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_ERROR)) {
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

    private void registerAccount(String login, String pass, String masterKey) {
        Bundle registrationData = new Bundle();
        registrationData.putString("requestType", "registration");
        registrationData.putString("login", login);
        registrationData.putString("password", pass);
        registrationData.putString("masterKey", masterKey);
        requestHander.SendDataToServer(registrationData);
    }
}
