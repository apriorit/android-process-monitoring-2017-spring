package com.apriorit.android.processmonitoring;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.apriorit.android.processmonitoring.registration.SharedPreferencesHandler;

public class Lock extends AppCompatActivity {

    private EditText mInputMasterKey;
    private SharedPreferencesHandler mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        mSharedPref = new SharedPreferencesHandler(this);
        mInputMasterKey = (EditText) findViewById(R.id.inputKey);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
    }

    //переопределение кнопки "Back" на сворачивание всех окон
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void unlockApp(View v) {
        String masterKey = mInputMasterKey.getText().toString();
        String correctKey = mSharedPref.getMasterKey();

        //check if user entered correct master key
        if(masterKey.equals(correctKey)) {
            Intent intentUpdateAccessibility = new Intent("UPDATE_BLACKLIST");
            intentUpdateAccessibility.putExtra("update_type", "once");
            sendBroadcast(intentUpdateAccessibility);
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "Wrong key!", Toast.LENGTH_LONG).show();
        }
    }
}
