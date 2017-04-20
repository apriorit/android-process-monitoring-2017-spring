package com.apriorit.android.processmonitoring;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.apriorit.android.processmonitoring.database.AppData;
import com.apriorit.android.processmonitoring.database.DatabaseHandler;
import com.apriorit.android.processmonitoring.device_administrator.PolicyManager;
import com.apriorit.android.processmonitoring.request_handler.Handler;
import com.apriorit.android.processmonitoring.select_device.SelectDeviceActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private Handler requestHander;
    private PolicyManager mPolicyManager;

    private EditText mEditLogin;
    private EditText mEditPassword;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditLogin = (EditText) findViewById(R.id.editTextLogin);
        mEditPassword = (EditText) findViewById(R.id.editTextPassword);

        gcmRegistration();

        initSQLiteDatabaseBlacklist();
        requestHander = new Handler(this);

        mPolicyManager = new PolicyManager(this);
    }

    private void initSQLiteDatabaseBlacklist() {
        DatabaseHandler db = new DatabaseHandler(this);
        db.addApplicationData(new AppData("com.example.admin.event", "Event", 1));
        db.addApplicationData(new AppData("com.android.settings", "Settings", 1));
        db.addApplicationData(new AppData("com.android.packageinstaller", "PackageInstaller", 1));
    }

    /*
      Registration device in GCM
      Obtains new device token
     */
    private void gcmRegistration() {
        //This is the handler that will manager to process the broadcast intent
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //check type of intent filter
                if (intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_ERROR)) {
                    //Registration error
                    Toast.makeText(getApplicationContext(), "GCM registration error!!!", Toast.LENGTH_LONG).show();
                }
            }
        };
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        //Check status of Google play service in device
        int resultCode = googleAPI.isGooglePlayServicesAvailable(getApplicationContext());
        if (ConnectionResult.SUCCESS != resultCode) {
            //Check type of error
            if (googleAPI.isUserResolvableError(resultCode)) {
                Toast.makeText(getApplicationContext(), "Google Play Service is not install/enable in this device!", Toast.LENGTH_LONG).show();
                googleAPI.showErrorNotification(getApplicationContext(), resultCode);
            } else {
                Toast.makeText(getApplicationContext(), "This device does not support for Google Play Service!", Toast.LENGTH_LONG).show();
            }
        } else {
            //Start service
            Intent intent = new Intent(this, GCMRegistrationIntentService.class);
            startService(intent);
        }
    }

    /**
     * Sends some registration data to GCM server
     * XMPP server saves in database user data and device token
     */
    public void registerAccount(View v) {
        Bundle registrationData = new Bundle();
        registrationData.putString("requestType", "account_registration");
        registrationData.putString("login", mEditLogin.getText().toString());
        registrationData.putString("password", mEditPassword.getText().toString());
        requestHander.SendDataToServer(registrationData);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_SUCCESS));

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_ERROR));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void HideApplication(View v) {
        //hide an application icon from Android applications list
        PackageManager pm = getApplicationContext().getPackageManager();
        pm.setComponentEnabledSetting(getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    public void controlCurrentDevice(View v) {
        Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
        intent.putExtra("mode", "children");
        intent.putExtra("login", "Dmitry");
        MainActivity.this.startActivity(intent);
    }
    public void controlOtherDevices(View v) {
        Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
        intent.putExtra("mode", "parent");
        intent.putExtra("login", "Dmitry");
        MainActivity.this.startActivity(intent);
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

