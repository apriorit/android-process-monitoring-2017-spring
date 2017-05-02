package com.apriorit.android.processmonitoring.registration;

import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import com.apriorit.android.processmonitoring.R;
import com.apriorit.android.processmonitoring.device_administrator.PolicyManager;
import com.apriorit.android.processmonitoring.request_handler.Handler;

public class SetSettingsActivity extends AppCompatActivity {
    private PolicyManager mPolicyManager;
    private SharedPreferencesHandler mSharedPref;
    private CheckBox mCheckBoxHideApp;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_settings);

        mCheckBoxHideApp = (CheckBox) findViewById(R.id.checkbox_hide_app);
        mSharedPref = new SharedPreferencesHandler(this);
        mPolicyManager = new PolicyManager(this);

        mHandler = new Handler(this);
    }

    public void finishConfiguring(View v) {
        mSharedPref.setAccessibilityState("activate");
        if (mCheckBoxHideApp.isChecked()) {
            mHandler.setEnabledSettings(false);
        }
        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
