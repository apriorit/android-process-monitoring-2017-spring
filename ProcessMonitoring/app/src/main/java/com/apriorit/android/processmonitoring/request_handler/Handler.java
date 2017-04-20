package com.apriorit.android.processmonitoring.request_handler;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.apriorit.android.processmonitoring.R;
import com.apriorit.android.processmonitoring.database.AppData;
import com.apriorit.android.processmonitoring.database.DatabaseHandler;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Handler {
    private Context mContext;

    public Handler(Context context) {
        mContext = context;
    }

    /**
     * Gets list of installed  applications and sends it to GCM server
     */
    public void sendDeviceInfo(int userID) {
        Bundle data = new Bundle();
        DatabaseHandler db = new DatabaseHandler(mContext);
        data.putString("requestType", "save-device-info");
        data.putString("user-id", Integer.toString(userID));

        List<ResolveInfo> listApps = getListApps();
        try {
            JSONObject jsonDeviceInfo;
            //add this list to database
            for (int i = 0; i < listApps.size(); i++) {
                db.addApplicationData(new AppData(listApps.get(i).activityInfo.packageName, listApps.get(i).loadLabel(mContext.getPackageManager()).toString(), 0));
            }
            List<AppData> blackList = db.getAllApps();
            for (AppData app : blackList) {
                jsonDeviceInfo = new JSONObject();
                jsonDeviceInfo.put("app-name", app.getAppName());
                jsonDeviceInfo.put("isblocked", app.isBlocked());
                data.putString(app.getPackageName(), jsonDeviceInfo.toString());
            }
            SendDataToServer(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void requestListApps(String userID) {
        Bundle data = new Bundle();
        data.putString("requestType", "get-list-apps");
        data.putString("user-id", userID);
        SendDataToServer(data);
    }
    public void deleteDevice(int userID) {
        Bundle data = new Bundle();
        data.putString("requestType", "delete-device");
        data.putString("user-id", String.valueOf(userID));
        SendDataToServer(data);
    }
    /**
     * Gets coordinates and sends it to GCM server
     */
    public void HandleDeviceLocation() {

    }

    /**
     * Returns a list of applications  installed on Android device
     */
    private List<ResolveInfo> getListApps() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        return mContext.getPackageManager().queryIntentActivities(mainIntent, 0);
    }

    /*
        Updates SQLite database which stores blacklist
    */
    public void updateBlacklistInDB(String jsonUpdatedBlacklist) {
        DatabaseHandler db = new DatabaseHandler(mContext);
        db.clearBlacklist();
        try {
            //parse json string
            JSONObject jsonObj = new JSONObject(jsonUpdatedBlacklist);
            Map<String, Object> mSourceListApps = JsonHelper.toMap(jsonObj);
            for (Map.Entry<String, Object> entry : mSourceListApps.entrySet()) {
                JSONObject jsonListApps = new JSONObject(entry.getValue().toString());
                String appName = (String) jsonListApps.get("app-name");
                int isBlocked = jsonListApps.getInt("isblocked");
                db.addApplicationData(new AppData(entry.getKey(), appName, isBlocked));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends data to GCM server
     * GCM server will deliver it to our XMPP server
     */
    public void SendDataToServer(final Bundle data) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    AtomicInteger msgId = new AtomicInteger();
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(mContext);
                    gcm.send(mContext.getResources().getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com", msgId.toString(), data);
                    return null;
                } catch (IOException ex) {
                    return "Error sending upstream message:" + ex.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    Log.d("Handler", "send message failed");
                }
            }
        }.execute(null, null, null);
    }
}

