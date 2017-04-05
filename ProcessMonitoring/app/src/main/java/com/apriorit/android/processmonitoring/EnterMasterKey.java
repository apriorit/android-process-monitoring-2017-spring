package com.apriorit.android.processmonitoring;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class EnterMasterKey extends AppCompatActivity {
    public static final String APP_PREFERENCES = "preference";
    public static final String APP_PREFERENCES_KEY = "masterKey";
    public static final int MIN_KEY_LENGHT = 4;
    SharedPreferences masterKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_master_key);

        masterKey = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        final TextView key = (TextView) findViewById(R.id.master_key);
        final TextView keyVetify = (TextView) findViewById(R.id.master_key_verify);
        Button next = (Button) findViewById(R.id.enterKey);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyText = key.getText().toString();
                String keyTextVerify = keyVetify.getText().toString();
                if((key.length() >=  MIN_KEY_LENGHT ) && (!key.equals(""))  && (keyTextVerify.equals(keyText)) ){
                    SharedPreferences.Editor e = masterKey.edit();
                    e.putString(APP_PREFERENCES_KEY, keyTextVerify);
                    e.commit();
                    Intent intent = new Intent(EnterMasterKey.this, Lock.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

            }
        });

    }
}
