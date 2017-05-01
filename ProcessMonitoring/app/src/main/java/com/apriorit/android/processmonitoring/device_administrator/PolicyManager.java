package com.apriorit.android.processmonitoring.device_administrator;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

/**
 * Managing policies enforced on a device
 * Activate device administrator mode for this application
 */
public class PolicyManager {

    public static final int DPM_ACTIVATION_REQUEST_CODE = 100;

    private DevicePolicyManager mDPM;
    private ComponentName adminComponent;

    public PolicyManager(Context context) {
        mDPM = (DevicePolicyManager) context
                .getSystemService(Context.DEVICE_POLICY_SERVICE);
        adminComponent = new ComponentName(context.getPackageName(),
                context.getPackageName() + ".device_administrator.DeviceAdministratorReceiver");
    }

    //Return true if the given administrator component is currently active (enabled) in the system
    public boolean isAdminActive() {
        return mDPM.isAdminActive(adminComponent);
    }

    public ComponentName getAdminComponent() {
        return adminComponent;
    }

    //Remove a current administration component
    public void disableAdmin() {
        mDPM.removeActiveAdmin(adminComponent);
    }

    public void Lock() {
        mDPM.lockNow();
    }

}
