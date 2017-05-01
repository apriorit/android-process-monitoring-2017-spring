package com.apriorit.android.processmonitoring.select_device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import com.apriorit.android.processmonitoring.R;
import com.apriorit.android.processmonitoring.device_management.DeviceManagementActivity;
import com.apriorit.android.processmonitoring.request_handler.Handler;
import com.apriorit.android.processmonitoring.request_handler.JsonHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.DialogFragment;

public class SelectDeviceActivity extends AppCompatActivity {
    private Handler mRequestHander;

    private String mMode;
    private String mLogin;

    private CheckBox mCheckBoxHideApp;
    private List<DeviceModel> mListDevices;
    private ListView mListViewDevices;

    private DialogFragment mDialogNewDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device);

        mCheckBoxHideApp = (CheckBox) findViewById(R.id.checkbox_hide_app);

        mDialogNewDevice = new DialogAddDevice();
        mRequestHander = new Handler(this);
        mListDevices = new ArrayList<>();
        mListViewDevices = (ListView) findViewById(R.id.listViewDevices);

        Intent intent = getIntent();
        //defines if user will control other devices
        mMode = intent.getStringExtra("mode");
        //login
        mLogin = intent.getStringExtra("login");

        requestListDevices();
    }

    private void requestListDevices() {
        Bundle registrationData = new Bundle();
        registrationData.putString("requestType", "get-list-devices");
        registrationData.putString("login", mLogin);
        mRequestHander.SendDataToServer(registrationData);
    }

    private void showListDevices(Intent intent) {
        mListDevices.clear();
        try {
            //parse json string
            JSONObject jsonObj = new JSONObject(intent.getStringExtra("list-devices"));
            Map<String, Object> mapListUsers = JsonHelper.toMap(jsonObj);

            for (Map.Entry<String, Object> entry : mapListUsers.entrySet()) {
                mListDevices.add(new DeviceModel(entry.getValue().toString(), Integer.parseInt(entry.getKey())));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!mListDevices.isEmpty()) {
            //Set adapter and show list
            DeviceListViewAdapter mListViewAdapter = new DeviceListViewAdapter(this, mListDevices);
            mListViewDevices.setAdapter(mListViewAdapter);

            mListViewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    int userID = mListDevices.get(position).getUserID();
                    if (mMode.equals("parent")) {
                        Intent intent = new Intent(SelectDeviceActivity.this, DeviceManagementActivity.class);
                        intent.putExtra("user-id", Integer.toString(userID));
                        SelectDeviceActivity.this.startActivity(intent);
                    } else {
                        sendDeviceInfoToServer(userID);
                        if (mCheckBoxHideApp.isChecked()) {
                            mRequestHander.setEnabledSettings(false);
                        }
                        //open settings to enable accessibility service
                        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivityForResult(intent, 0);
                    }
                }
            });
        }
    }

    private void sendDeviceInfoToServer(int userID) {
        mRequestHander.sendDeviceInfo(userID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter("LIST_DEVICES"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    public void openDialogFragment(View v) {
        Bundle args = new Bundle();
        args.putString("login", mLogin);
        mDialogNewDevice.setArguments(args);
        mDialogNewDevice.show(getFragmentManager(), "dialog");
    }

    //Receives list with devices in order to display in list view
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showListDevices(intent);
        }
    };
}
