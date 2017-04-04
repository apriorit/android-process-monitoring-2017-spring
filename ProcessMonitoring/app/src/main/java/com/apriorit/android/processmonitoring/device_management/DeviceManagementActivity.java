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
    private Map<String, Object> mSourceListApps;
    AppsListViewAdapter mListViewAdapter;

    boolean initListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_management);

        mListAppDataModel = new ArrayList<>();

        initListView = false;
        mListViewAdapter = null;
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
        try {
            //parse json string
            JSONObject jsonObj = new JSONObject(intent.getStringExtra("list"));
            mSourceListApps = JsonHelper.toMap(jsonObj);
            for (Map.Entry<String, Object> entry : mSourceListApps.entrySet()) {
                if (mListViewAdapter == null) {
                    mListAppDataModel.add(new AppDataModel(entry.getValue().toString(), false));
                } else {
                    mListAppDataModel.add(new AppDataModel(entry.getValue().toString(), false));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(!initListView) {
            //Set adapter and show list
            mListViewAdapter = new AppsListViewAdapter(this, mListAppDataModel);

            // Set the list
            mListViewApps = (ListView) findViewById(R.id.listViewApps);
            mListViewApps.setAdapter(mListViewAdapter);

            initListView = true;
        } else {
            mListViewAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Send list of blocked apps to other device via GCM and App server
     */
    public void sendBlacklist(View v) {
        Bundle blockedApps = new Bundle();
        blockedApps.putString("requestType", "update-blacklist");
        //Adding list of blocked apps to bundle
        int i = 0;
        for (Map.Entry<String, Object> entry : mSourceListApps.entrySet()) {
            AppDataModel app = (AppDataModel) mListViewApps.getItemAtPosition(i);
            if (app.getAccess()) {
                blockedApps.putString(entry.getKey(), entry.getValue().toString());
            }
            i++;
        }
        //Send list of blocked apps to server
        if (i != 0)
            requestHander.SendDataToServer(blockedApps);
    }
}