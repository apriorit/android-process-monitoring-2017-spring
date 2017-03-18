package com.apriorit.android.processmonitoring.request_handler;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.apriorit.android.processmonitoring.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.List;
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
    private void HandleListApps() {
        Bundle data = new Bundle();
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

