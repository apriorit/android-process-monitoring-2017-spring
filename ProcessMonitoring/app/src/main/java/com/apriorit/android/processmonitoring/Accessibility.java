package com.apriorit.android.processmonitoring;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.Settings;

import android.content.IntentFilter;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.apriorit.android.processmonitoring.database.AppData;
import com.apriorit.android.processmonitoring.database.DatabaseHandler;

import java.util.Iterator;
import java.util.List;


public class Accessibility extends AccessibilityService {
    private static final String TAG = "AccessibiltiyService";
    private AccessibilityServiceInfo info;
    //allows to launch application only once
    private boolean mAllowLaunchApp = false;

    private String querySettingPkgName() {
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfos == null || resolveInfos.size() == 0) {
            return "";
        }
        return resolveInfos.get(0).activityInfo.packageName;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String app_name = getString(R.string.app_name);
        String accessibility_service_label = getString(R.string.accessibility_service_label);
        Log.d(TAG, accessibility_service_label);
        List<CharSequence> wordsInWindow = event.getText();
        Log.d(TAG, String.format("packageName: %s  className %s eventType %s text %s", event.getPackageName(), event.getClassName(), event.getEventType(), event.getText()));

        //name apps in settings
        Boolean flagIsLock = false;
        Iterator<CharSequence> iter = wordsInWindow.iterator();
        while (iter.hasNext()) {
            String word = (String) iter.next();
            Log.d(TAG, word);
            if (word.equals(app_name) || word.equals(accessibility_service_label)) {
                flagIsLock = true;

            }
        }

        String eventPackage = String.valueOf(event.getPackageName());
        Log.d(TAG, eventPackage);

        DatabaseHandler db = new DatabaseHandler(this);
        List<AppData> blackList = db.getAllApps();

        if (!mAllowLaunchApp) {
            //compare current application with blacklist
            for (AppData app : blackList) {
                if (app.isBlocked() == 1) {
                    if (eventPackage.equals(app.getPackageName()) && (app.getPackageName().equals(querySettingPkgName()) || app.getPackageName().equals("com.android.packageinstaller"))) {
                        if (flagIsLock) {
                            startLock(eventPackage);
                        }
                    } else {
                        if (eventPackage.equals(app.getPackageName())) {
                            startLock(eventPackage);
                        }
                    }
                }
            }
        }
        mAllowLaunchApp = false;
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt");
    }

    @Override
    protected void onServiceConnected() {

        super.onServiceConnected();
        info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | AccessibilityEvent.TYPE_VIEW_CLICKED;

        DatabaseHandler db = new DatabaseHandler(this);
        List<AppData> blackList = db.getAllApps();

        //Updates package names in AccessibilityServiceInfo
        info.packageNames = new String[db.getCountOfBlockedApps()];
        int k = 0;
        for (AppData app : blackList) {
            if (app.isBlocked() == 1) {
                info.packageNames[k] = app.getPackageName();
                k++;
            }
        }

        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        info.notificationTimeout = 100;
        this.setServiceInfo(info);

        //register Broadcastreceiver
        registerReceiver(receiver, new IntentFilter("UPDATE_BLACKLIST"));
    }

    //запускаем окно блокировки
    public void startLock(String packageName) {
        try {
            Intent intent = new Intent(Accessibility.this, Lock.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("packageName", packageName);
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Receives notification when blacklist was updated
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("update_type");
            if (type.equals("once")) {
                mAllowLaunchApp = true;
            } else {
                onServiceConnected();
            }
        }
    };

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
