package com.apriorit.android.processmonitoring;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.apriorit.android.processmonitoring.request_handler.Handler;
import com.google.android.gms.gcm.GcmListenerService;

public class GCMPushReceiverService extends GcmListenerService {
    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);
        Intent intentUpdateList;
        if (data != null)
            sendNotification(data.getString("LIST_APPS"));

        Handler handler = new Handler(this);
        try {
            String type = data.getString("requestType");
            if (type != null) {
                switch (type) {
                    case "list-apps":
                        //sends downstream message to activity
                        intentUpdateList = new Intent("LIST_APPS");
                        intentUpdateList.putExtra("list", data.getString("LIST_APPS"));
                        sendBroadcast(intentUpdateList);
                        break;
                    case "update-list":
                        handler.sendDeviceInfo(Integer.parseInt(data.getString("LIST_APPS")));
                        break;
                    case "location":
                        handler.HandleDeviceLocation();
                        break;
                    case "update-blacklist":
                        //update dababase
                        handler.updateBlacklistInDB(data.getString("LIST_APPS"));
                        intentUpdateList = new Intent("UPDATE_BLACKLIST");
                        //notificate accessibility service to update blacklist
                        sendBroadcast(intentUpdateList);
                        break;
                    case "list-devices":
                        intentUpdateList = new Intent("LIST_DEVICES");
                        intentUpdateList.putExtra("list-devices", data.getString("LIST_APPS"));
                        sendBroadcast(intentUpdateList);
                        break;
                    default:
                        break;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Enables specific activity
     * Shows an application icon in Android application list
     */
    public static void setActivityEnabled(Context context, final Class<? extends Activity> activityClass) {
        final PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(context, activityClass), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    /*
    Put the message into a notification.
    This code is used for testing GCM server
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int requestCode = 0;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);

        //Build notification
        NotificationCompat.Builder noBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("My GCM message :X:X")
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, noBuilder.build()); //0 = ID of notifications
    }
}
