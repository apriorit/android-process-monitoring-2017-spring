package com.apriorit.android.processmonitoring.device_management;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.apriorit.android.processmonitoring.R;
import com.apriorit.android.processmonitoring.request_handler.Handler;
import com.apriorit.android.processmonitoring.request_handler.JsonHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeviceManagementActivity extends AppCompatActivity {
    private List<AppDataModel> mListAppDataModel;

    private ListView mListViewApps;
    private Handler requestHander;
    private AppsListViewAdapter mListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_management);

        mListAppDataModel = new ArrayList<>();
        mListViewApps = (ListView) findViewById(R.id.listViewApps);

        requestHander = new Handler(this);

        Intent intent = getIntent();
        //some data from MainActivity
        String id_user = intent.getStringExtra("key");

        //Sends request to server
        requestHander.HandleListApps();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Registrate receiver which gets list with apps
        registerReceiver(broadcastReceiver, new IntentFilter("LIST_APPS"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    //Receives list with app in order to display in list view
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateListView(intent);
        }
    };

    /**
     * Sends list of apps to GCM server
     * GCM server will deliver it to our XMPP server
     */
    public void getListAppsFromDevice(View view) {
        requestHander.HandleListApps();
    }

    private void updateListView(Intent intent) {
        mListViewAdapter = null;
        try {
            //parse json string
            JSONObject jsonObj = new JSONObject(intent.getStringExtra("list"));
            Map<String, Object> mSourceListApps = JsonHelper.toMap(jsonObj);
            for (Map.Entry<String, Object> entry : mSourceListApps.entrySet()) {
                mListAppDataModel.add(new AppDataModel(entry.getKey(), entry.getValue().toString(), false));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Set adapter and show list
        mListViewAdapter = new AppsListViewAdapter(this, mListAppDataModel);

        mListViewApps.setAdapter(mListViewAdapter);
    }

    /**
     * Send list of blocked apps to other device via GCM and App server
     */
    public void sendBlacklist(View v) {
        Bundle blockedApps = new Bundle();
        blockedApps.putString("requestType", "update-blacklist");
        //Adding list of blocked apps to bundle
        for (int i = 0; i < mListViewAdapter.getCount(); i++) {
            AppDataModel app = (AppDataModel) mListViewAdapter.getItem(i);
            if (app.getAccess()) {
                blockedApps.putString(app.getPackageName(), app.getAppName());
            }
        }
        //Send list of blocked apps to server
        if (blockedApps.size() != 0)
            requestHander.SendDataToServer(blockedApps);
    }
}