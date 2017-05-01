package com.apriorit.android.processmonitoring.registration;

import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.apriorit.android.processmonitoring.R;
import com.apriorit.android.processmonitoring.device_administrator.PolicyManager;

public class SetSettingsActivity extends AppCompatActivity {

    private PolicyManager mPolicyManager;
    private SharedPreferencesHandler mSharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_settings);

        mSharedPref = new SharedPreferencesHandler(this);
        mPolicyManager = new PolicyManager(this);
    }
    public void activateAccessibility(View v) {
        mSharedPref.setAccessibilityState("activate");
        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    public void activateAdmin(View v) {
        if (!mPolicyManager.isAdminActive()) {
            Intent activateDeviceAdmin = new Intent(
                    DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            startActivity(activateDeviceAdmin);
        }
    }
}
