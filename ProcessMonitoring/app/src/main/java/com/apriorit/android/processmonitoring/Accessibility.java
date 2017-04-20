package com.apriorit.android.processmonitoring;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.apriorit.android.processmonitoring.database.AppData;
import com.apriorit.android.processmonitoring.database.DatabaseHandler;

import java.util.List;

public class Accessibility extends AccessibilityService {

    private AccessibilityServiceInfo info;
    static final String TAG = "RecorderService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String app_name = getString(R.string.app_name);
        String accessibility_service_label = getString(R.string.accessibility_service_label);
        Log.d(TAG, accessibility_service_label);
        List<CharSequence> wordsInWindow = event.getText();
        Log.d(TAG, String.format("packageName: %s  className %s eventType %s text %s", event.getPackageName(), event.getClassName(), event.getEventType(), event.getText()));
        String nameActivity = (String) wordsInWindow.get(0);
        Log.d(TAG, nameActivity);
        String eventPackage = String.valueOf(event.getPackageName());
        Log.d(TAG, eventPackage);
        if (!eventPackage.equals("com.android.settings")
                || nameActivity.equals(accessibility_service_label)
                || nameActivity.equals(app_name)
                ) {
            startLock();
        }
    }

    /**
     * Updates package names in AccessibilityServiceInfo
     */
    private void attachBlacklistToAccessibility() {
        DatabaseHandler db = new DatabaseHandler(this);
        List<AppData> blackList = db.getAllApps();

        info.packageNames = new String[blackList.size()];
        int k = 0;
        for (AppData app : blackList) {
            if(app.isBlocked() == 1) {
                info.packageNames[k] = app.getPackageName();
                k++;
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        attachBlacklistToAccessibility();

        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        info.notificationTimeout = 100;
        this.setServiceInfo(info);
        Log.d(TAG, "onServiceConnected");

        //register Broadcastreceiver
        registerReceiver(receiver, new IntentFilter("UPDATE_BLACKLIST"));
    }

    //запускаем окно блокировки
    public void startLock() {
        try {
            Intent intent = new Intent(Accessibility.this, Lock.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Receives notification when blacklist was updated
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onServiceConnected();
        }
    };

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
