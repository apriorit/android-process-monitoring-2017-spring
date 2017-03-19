package com.apriorit.android.processmonitoring;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Lock extends AppCompatActivity {
    public static final String APP_PREFERENCES = "preference";
    public static final String APP_PREFERENCES_KEY = "masterKey";
    SharedPreferences masterKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        masterKey = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        final TextView testKey = (TextView) findViewById(R.id.testKey);
        final TextView inputKey = (TextView) findViewById(R.id.inputKey);
        Button unlock = (Button) findViewById(R.id.unlock);

        //бработка ввода ключа
        unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String masterKeyText = masterKey.getString(APP_PREFERENCES_KEY,"0000");
                String inputKeyText = inputKey.getText().toString();

                if(inputKeyText.equals(masterKeyText)){
                    testKey.setText(masterKeyText);
                }
            }
        });
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
    }
    //переопределение кнопки "Back" на сворачивание всех окон
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
