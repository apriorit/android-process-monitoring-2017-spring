package com.apriorit.android.processmonitoring.lock;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.apriorit.android.processmonitoring.R;
import com.apriorit.android.processmonitoring.registration.SharedPreferencesHandler;
import com.apriorit.android.processmonitoring.request_handler.Handler;

public class EnableAppActivity extends AppCompatActivity {
    private EditText mInputKey;
    private EditText mInputPassword;

    private Handler mHandler;
    SharedPreferencesHandler mSharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enable_app);

        mInputKey = (EditText) findViewById(R.id.inputMasterKey);
        mInputPassword = (EditText) findViewById(R.id.inputMainPassword);

        mHandler = new Handler(this);
        mSharedPref = new SharedPreferencesHandler(this);
    }

    public void enableApp(View v) {
        String login = mSharedPref.getLogin();
        String masterKey = mSharedPref.getMasterKey();
        if(masterKey.equals(mInputKey.getText().toString())) {
            mHandler.requestEnableApp(login, mInputPassword.getText().toString());
        } else {
            Toast.makeText(getApplicationContext(), "Wrong key!", Toast.LENGTH_LONG).show();
        }
    }
}
