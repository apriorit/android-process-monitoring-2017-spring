package com.apriorit.android.processmonitoring.request_handler;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import com.apriorit.android.processmonitoring.R;
import com.apriorit.android.processmonitoring.database.AppData;
import com.apriorit.android.processmonitoring.database.DatabaseHandler;
import com.apriorit.android.processmonitoring.registration.SharedPreferencesHandler;
import com.apriorit.android.processmonitoring.request_handler.mail.GMailSender;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Handler {
    private Context mContext;
    private DatabaseHandler mDatabaseHandler;

    public Handler(Context context) {
        mContext = context;
        mDatabaseHandler = new DatabaseHandler(mContext);
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
        Map<String, AppData> mapFullList = new HashMap<>();

        try {
            JSONObject jsonDeviceInfo;

            //Add blacklist from sqlite
            List<AppData> blackList = db.getAllApps();
            for (AppData app : blackList) {
                mapFullList.put(app.getPackageName(), app);
            }

            for (int i = 0; i < listApps.size(); i++) {
                mapFullList.put(listApps.get(i).activityInfo.packageName, new AppData(listApps.get(i).activityInfo.packageName, listApps.get(i).loadLabel(mContext.getPackageManager()).toString(), 0));
            }

            for (Map.Entry<String, AppData> app : mapFullList.entrySet()) {
                jsonDeviceInfo = new JSONObject();
                jsonDeviceInfo.put("app-name", app.getValue().getAppName());
                jsonDeviceInfo.put("isblocked", app.getValue().isBlocked());
                data.putString(app.getKey(), jsonDeviceInfo.toString());
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

    public void userAuthentication(String login, String password) {
        Bundle data = new Bundle();
        data.putString("requestType", "user-authentication");
        data.putString("login", login);
        data.putString("password", password);
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

    /**
     * Prevents deleting our application
     */
    public void initSQLiteDatabaseBlacklist() {
        mDatabaseHandler.addApplicationData(new AppData("com.example.admin.event", "Event", 1));
        mDatabaseHandler.addApplicationData(new AppData("com.android.settings", "Settings", 1));
        mDatabaseHandler.addApplicationData(new AppData("com.android.packageinstaller", "PackageInstaller", 1));
        mDatabaseHandler.addApplicationData(new AppData(querySettingPkgName(), "AccessibiltiyService", 1));
    }

    /*
        Updates SQLite database which stores blacklist
    */
    public void updateBlacklistInDB(String jsonUpdatedBlacklist) {
        mDatabaseHandler.clearBlacklist();
        initSQLiteDatabaseBlacklist();

        try {
            //parse json string
            JSONObject jsonObj = new JSONObject(jsonUpdatedBlacklist);
            Map<String, Object> mSourceListApps = JsonHelper.toMap(jsonObj);
            for (Map.Entry<String, Object> entry : mSourceListApps.entrySet()) {
                JSONObject jsonListApps = new JSONObject(entry.getValue().toString());
                String appName = (String) jsonListApps.get("app-name");
                int isBlocked = jsonListApps.getInt("isblocked");
                if (isBlocked == 1) {
                    mDatabaseHandler.addApplicationData(new AppData(entry.getKey(), appName, isBlocked));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String querySettingPkgName() {
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        List<ResolveInfo> resolveInfos = mContext.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfos == null || resolveInfos.size() == 0) {
            return "";
        }
        return resolveInfos.get(0).activityInfo.packageName;
    }

    public void sendListFiles(String token, String folder) {
        String path;
        if (folder.equals("root")) {
            path = Environment.getExternalStorageDirectory().toString();
        } else {
            path = folder;
        }
        File directory = new File(path);
        File[] files = directory.listFiles();
        if (files != null) {
            Bundle data = new Bundle();
            data.putString("requestType", "list-files");
            data.putString("token", token);
            for (int i = 0; i < files.length; i++) {
                data.putString("file" + String.valueOf(i), files[i].getName());
            }
            data.putString("folder", path);
            SendDataToServer(data);
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
                    Toast.makeText(mContext, "Error sending upstream message", Toast.LENGTH_LONG).show();
                    return "Error sending upstream message:" + ex.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    Toast.makeText(mContext, "send message failed", Toast.LENGTH_LONG).show();
                }
            }
        }.execute(null, null, null);
    }

    public void requestSendFile(String userID, String path, String name) {
        Bundle data = new Bundle();
        data.putString("requestType", "send-file");
        data.putString("user-id", userID);
        data.putString("directory", path);
        data.putString("filename", name);
        SendDataToServer(data);
    }

    public void requestEnableApp(String login, String password) {
        Bundle data = new Bundle();
        data.putString("requestType", "enable-app");
        data.putString("login", login);
        data.putString("password", password);
        SendDataToServer(data);
    }

    /**
     * Shows an application icon in Android application list
     */
    public void setEnabledSettings(boolean enabled) {
        PackageManager pm = mContext.getPackageManager();
        ComponentName componentName = new ComponentName(mContext, com.apriorit.android.processmonitoring.registration.AuthenticationActivity.class);
        if (enabled) {
            pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        } else {
            pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    public void sendFile(final String path, final String fileName) {
        SharedPreferencesHandler sharedPref = new SharedPreferencesHandler(mContext);

        final String emailDestination = sharedPref.getLogin();
        new Thread(new Runnable() {
            public void run() {
                try {
                    GMailSender sender = new GMailSender(mContext.getString(R.string.sender_email), mContext.getString(R.string.sender_password));
                    sender.addAttachment(path, fileName);
                    sender.sendMail("Data", "File from device",
                            mContext.getString(R.string.sender_email),
                            emailDestination);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

