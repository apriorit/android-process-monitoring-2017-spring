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
     * Uses Chain of Responsibility pattern to choose handler for current request
     */
    public void HandleRequest(String request) {
        switch(request) {
            case "list-apps":
                HandleListApps();
                break;
            case "location":
                HandleDeviceLocation();
                break;
            default:
                break;
        }
    }

    /**
     * Gets list of installed  applications and sends it to GCM server
     */
    public void HandleListApps() {
        Bundle data = new Bundle();
        data.putString("requestType", "getAppsList");
        List<ResolveInfo> listApps = getListApps();
        for(int i = 0; i < listApps.size(); i++) {
            data.putString(listApps.get(i).activityInfo.packageName, listApps.get(i).loadLabel(mContext.getPackageManager()).toString());
        }
        SendDataToServer(data);
    }

    /**
     * Gets coordinates and sends it to GCM server
     */
    private void HandleDeviceLocation() {

    }

    /**
     *  Returns a list of applications  installed on Android device
     */
    private List<ResolveInfo> getListApps() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        return mContext.getPackageManager().queryIntentActivities( mainIntent, 0);
    }
    /*
        Updates SQLite database
    */
    public void updateBlacklist(String jsonList) {
        DatabaseHandler db = new DatabaseHandler(mContext);
        Log.d("List with apps in json ", jsonList);
        try {
            JSONObject jsonObj = new JSONObject(jsonList);
            Map<String, Object> blackListMap = JsonHelper.toMap(jsonObj);
            for (Map.Entry<String, Object> entry : blackListMap.entrySet()) {
                db.addApplicationData(new AppData(entry.getKey(), entry.getValue().toString()));
            }
        } catch(JSONException e) {

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

