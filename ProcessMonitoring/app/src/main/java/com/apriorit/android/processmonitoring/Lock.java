package com.apriorit.android.processmonitoring;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.apriorit.android.processmonitoring.database.DatabaseHandler;

public class Lock extends AppCompatActivity {
    private String mPackageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        Intent intent = getIntent();
        mPackageName = intent.getStringExtra("packageName");
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
        DatabaseHandler db = new DatabaseHandler(this);
        db.deleteAppByPackage(mPackageName);
        finish();
    }
}
