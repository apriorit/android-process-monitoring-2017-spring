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
import com.apriorit.android.processmonitoring.device_administrator.PolicyManager;
import com.apriorit.android.processmonitoring.lock.EnableAppActivity;
import com.apriorit.android.processmonitoring.registration.SharedPreferencesHandler;

import java.util.Iterator;
import java.util.List;


public class Accessibility extends AccessibilityService {
    private static final String TAG = "AccessibilityService";
    private AccessibilityServiceInfo info;

    //allows to launch application only once
    private String mCurrentUnlockedApp;

    private boolean mDisableAccessibilityService = false;
    private String mInitWindowWithSettings = null;
    private boolean mBlockSettings = false;
    private String mPackagePhoneSettings;
    private SharedPreferencesHandler mSharedPref;
    private DatabaseHandler mDatabaseHandler;
    private PolicyManager mPolicyManager;

    private String getSettingsPackageName1() {
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfos == null || resolveInfos.size() == 0) {
            return "";
        }
        return resolveInfos.get(0).activityInfo.packageName;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
       if(mDisableAccessibilityService) {
            return;
        }
        String app_name = getString(R.string.app_name);
        String accessibility_service_label = getString(R.string.accessibility_service_label);
        List<CharSequence> wordsInWindow = event.getText();

        Boolean flagIsLock = false;
        try {
            Iterator<CharSequence> iter = wordsInWindow.iterator();
            while (iter.hasNext()) {
                String word = (String) iter.next();
                Log.d(TAG, word);
                if (word.equals(app_name) || word.equals(accessibility_service_label)) {
                    flagIsLock = true;
                }
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String eventPackage = String.valueOf(event.getPackageName());
        String state = mSharedPref.getAccessibiltiyState();

        if (state == null) {
            return;
        }

        if (state.equals("activate")) {
            if (mInitWindowWithSettings == null) {
                if (eventPackage.equals(mPackagePhoneSettings)) {
                    mInitWindowWithSettings = event.getText().toString();
                }
            }
        }

        if (mInitWindowWithSettings == null) {
            return;
        }

        if (mBlockSettings) {
            if (eventPackage.equals(mPackagePhoneSettings) && mInitWindowWithSettings.equals(event.getText().toString())) {
                mBlockSettings = false;
            }
            //if we open settings but not the main window
            if (eventPackage.equals(mPackagePhoneSettings) && !mInitWindowWithSettings.equals(event.getText().toString())) {
                startLockSettings();
                try {
                    mPolicyManager.Lock();
                } catch(SecurityException e) {
                    e.printStackTrace();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

        List<AppData> blackList = mDatabaseHandler.getAllApps();

        if (mCurrentUnlockedApp != null) {
            if (!mCurrentUnlockedApp.equals(eventPackage) && !eventPackage.equals(getPackageName())) {
                mCurrentUnlockedApp = null;
            }
        }

        //compare current application with blacklist
        for (AppData app : blackList) {
            if (app.isBlocked() == 1) {
                if (eventPackage.equals(app.getPackageName()) && (app.getPackageName().equals(mPackagePhoneSettings) || app.getPackageName().equals("com.android.packageinstaller"))) {
                    if (flagIsLock) {
                        mBlockSettings = true;
                        startLockSettings();
                        try {
                            mPolicyManager.Lock();
                        } catch(SecurityException e) {
                            e.printStackTrace();
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (eventPackage.equals(app.getPackageName())) {
                        if (mCurrentUnlockedApp == null) {
                            startLock(eventPackage);
                        }
                    }
                }
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
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | AccessibilityEvent.TYPE_VIEW_CLICKED;

        mDatabaseHandler = new DatabaseHandler(this);


        mPolicyManager = new PolicyManager(this);

        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        info.notificationTimeout = 100;
        this.setServiceInfo(info);

        //register BroadcastReceiver
        registerReceiver(receiver, new IntentFilter("UPDATE_BLACKLIST"));

        mSharedPref = new SharedPreferencesHandler(this);
        if (mSharedPref.getAccessibiltiyState() == null) {
            mSharedPref.setAccessibilityState("enabled");
        }
        mPackagePhoneSettings = getSettingsPackageName1();
        mDisableAccessibilityService = false;
    }

    public void startLockSettings() {
        try {
            Intent intent = new Intent(Accessibility.this, EnableAppActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
            String requestDisableService = intent.getStringExtra("disable");
            if (requestDisableService != null) {
                if (requestDisableService.equals("update_list")) {
                    onServiceConnected();
                } else {
                    if (requestDisableService.equals("accessibility")) {
                        mDisableAccessibilityService = true;
                    } else {
                        mCurrentUnlockedApp = requestDisableService;
                    }
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
