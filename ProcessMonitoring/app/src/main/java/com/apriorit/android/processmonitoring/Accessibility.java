package com.apriorit.android.processmonitoring;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.List;


public class Accessibility extends AccessibilityService{

    static final String TAG = "RecorderService";
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG,"StartEvent");
        List<CharSequence> wordsInWindow = event.getText();
        Log.d(TAG, String.format("packageName: %s  className %s eventType %s text %s" , event.getPackageName(), event.getClassName(), event.getEventType(),event.getText()));
       String nameActivity = (String) wordsInWindow.get(0);
        Log.d(TAG, nameActivity);
        String eventPackage = String.valueOf(event.getPackageName());
        Log.d(TAG, eventPackage);
        if(!eventPackage.equals("com.android.settings")
                ||nameActivity.equals("Accessibility")
                ||nameActivity.equals("ProcessMonitoring")
                )
        {
            startLock();

        }

    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "onServiceConnected");
    }


    //запускаем окно блокировки
    public void startLock(){
        try {
            Intent intent = new Intent(Accessibility.this, Lock.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
