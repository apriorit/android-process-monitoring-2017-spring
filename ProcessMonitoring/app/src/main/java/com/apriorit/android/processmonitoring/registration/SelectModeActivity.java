package com.apriorit.android.processmonitoring.registration;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.apriorit.android.processmonitoring.R;
import com.apriorit.android.processmonitoring.request_handler.Handler;
import com.apriorit.android.processmonitoring.select_device.SelectDeviceActivity;

public class SelectModeActivity extends AppCompatActivity {
    private String mLogin;
    private SharedPreferencesHandler mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_mode);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        mLogin = intent.getStringExtra("login");

        mSharedPref = new SharedPreferencesHandler(this);

        Handler requestHander = new Handler(this);
        requestHander.initSQLiteDatabaseBlacklist();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sign_out) {
            mSharedPref.clearData();
            mSharedPref.getLogin();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void controlThisDevice(View v) {
        Intent intent = new Intent(SelectModeActivity.this, SelectDeviceActivity.class);
        intent.putExtra("mode", "children");
        intent.putExtra("login", mLogin);
        SelectModeActivity.this.startActivity(intent);
    }

    public void controlOtherDevices(View v) {
        Intent intent = new Intent(SelectModeActivity.this, SelectDeviceActivity.class);
        intent.putExtra("mode", "parent");
        intent.putExtra("login", mLogin);
        SelectModeActivity.this.startActivity(intent);
    }
}
