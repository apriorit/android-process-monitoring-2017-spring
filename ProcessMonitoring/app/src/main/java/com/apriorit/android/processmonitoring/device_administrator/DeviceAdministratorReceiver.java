package com.apriorit.android.processmonitoring.device_administrator;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Receives notifications when user enables or disables Device administrator
 */
public class DeviceAdministratorReceiver extends DeviceAdminReceiver {

    /**
     * Action sent to a device administrator when the user has disabled it
     */
	@Override
	public void onDisabled(Context context, Intent intent) {
		Toast.makeText(context, "onDisabled", Toast.LENGTH_SHORT).show();
		super.onDisabled(context, intent);
	}

    /**
     * Action sent to a device administrator when the user has enabled it
     */
    @Override
    public void onEnabled(Context context, Intent intent) {
        Toast.makeText(context, "enabled dpm", Toast.LENGTH_SHORT).show();
        super.onEnabled(context, intent);
    }

    /**
     * Action sent to a device administrator when the user has requested to disable it,
     * but before this has actually been done
     */
    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        Toast.makeText(context, "onDisableRequested", Toast.LENGTH_SHORT).show();
        Intent intentSettings = new Intent(android.provider.Settings.ACTION_SETTINGS);
        intentSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.getApplicationContext().startActivity(intentSettings);

        //Show dialog window
        return "You shouldn't disable administrator mode";
    }
}
