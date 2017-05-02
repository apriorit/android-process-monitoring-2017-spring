package com.apriorit.android.processmonitoring;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.apriorit.android.processmonitoring.database.AppData;
import com.apriorit.android.processmonitoring.database.DatabaseHandler;
import com.apriorit.android.processmonitoring.device_administrator.PolicyManager;

import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    private PolicyManager mPolicyManager;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPolicyManager = new PolicyManager(this);
    }

    public void HideApplication(View v) {
        //hide an application icon from Android applications list
        PackageManager pm = getApplicationContext().getPackageManager();
        pm.setComponentEnabledSetting(getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    public void ShowBlacklist(View v) {
        DatabaseHandler db = new DatabaseHandler(this);
        List<AppData> blackList = db.getAllApps();

        String[] mBlacklist = new String[blackList.size()];
        int k = 0;
        for (AppData app : blackList) {
            mBlacklist[k] = app.getPackageName() + " " + app.getAppName();
            k++;
        }
        // Find the list
        ListView listViewApps = (ListView) findViewById(R.id.listViewBlacklist);

        // Creates adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, mBlacklist);

        // присваиваем адаптер списку
        listViewApps.setAdapter(adapter);
    }

    //Handles click on buttons Activate and deactivate admin
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.activate_admin:
                //Activates Device administrator
                if (!mPolicyManager.isAdminActive()) {
                    Intent activateDeviceAdmin = new Intent(
                            DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);

                    activateDeviceAdmin.putExtra(
                            DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                            mPolicyManager.getAdminComponent());
                    activateDeviceAdmin
                            .putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                    "After activating admin, you will be able to block application uninstallation.");
                    startActivityForResult(activateDeviceAdmin,
                            PolicyManager.DPM_ACTIVATION_REQUEST_CODE);
                }
                break;
            case R.id.deactivate_admin:
                //disabling device admin
                if (mPolicyManager.isAdminActive()) {
                    mPolicyManager.disableAdmin();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK
                && requestCode == PolicyManager.DPM_ACTIVATION_REQUEST_CODE) {
            // handle code for successfull enable of admin
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}

