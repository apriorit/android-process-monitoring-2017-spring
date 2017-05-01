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
import com.apriorit.android.processmonitoring.device_management.view_files.ViewFilesActivity;
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

    private String mUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_management);

        mListAppDataModel = new ArrayList<>();
        mListViewApps = (ListView) findViewById(R.id.listViewApps);

        requestHander = new Handler(this);

        Intent intent = getIntent();
        //some data from MainActivity
        mUserID = intent.getStringExtra("user-id");

        //Sends request to server
        requestHander.requestListApps(mUserID);
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
     * request list with apps
     */
    public void updateList(View view) {
        Bundle data = new Bundle();
        data.putString("requestType", "update-list");
        data.putString("user-id", mUserID);
        requestHander.SendDataToServer(data);
    }

    private void updateListView(Intent intent) {
        mListViewAdapter = null;
        try {
            //parse json string
            JSONObject jsonObj = new JSONObject(intent.getStringExtra("list"));
            Map<String, Object> mSourceListApps = JsonHelper.toMap(jsonObj);
            for (Map.Entry<String, Object> entry : mSourceListApps.entrySet()) {
                JSONObject jsonAppData = new JSONObject(entry.getValue().toString());
                String aName = (String) jsonAppData.get("app-name");
                int isBlocked = jsonAppData.getInt("isblocked");
                mListAppDataModel.add(new AppDataModel(entry.getKey(), aName, isBlocked));
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
        blockedApps.putString("user-id", mUserID);
        JSONObject jsonAppData;
        try {
            //Adding list of blocked apps to bundle
            for (int i = 0; i < mListViewAdapter.getCount(); i++) {
                AppDataModel app = (AppDataModel) mListViewAdapter.getItem(i);
                jsonAppData = new JSONObject();
                jsonAppData.put("app-name", app.getAppName());
                jsonAppData.put("isblocked", app.getAccess());
                blockedApps.putString(app.getPackageName(), jsonAppData.toString());
            }
            //Send list of blocked apps to server
            if (blockedApps.size() != 0)
                requestHander.SendDataToServer(blockedApps);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void openViewFilesActivity(View v) {
        Intent intent = new Intent(DeviceManagementActivity.this, ViewFilesActivity.class);
        intent.putExtra("user-id", mUserID);
        DeviceManagementActivity.this.startActivity(intent);
    }
}