package com.apriorit.android.processmonitoring.registration;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.apriorit.android.processmonitoring.R;

public class SetSettingsActivity extends AppCompatActivity {

    private SharedPreferencesHandler mSharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_settings);

        mSharedPref = new SharedPreferencesHandler(this);

    }
    public void activateAccessibility(View v) {
        mSharedPref.setAccessibilityState("activate");
        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
