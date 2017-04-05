package com.apriorit.android.processmonitoring;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Registration extends AppCompatActivity {

    public static final String APP_PREFERENCES = "preference";
    public static final String APP_PREFERENCES_KEY = "masterKey";
    public static final int MIN_KEY_LENGHT = 4;
    SharedPreferences masterKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //save master key in phone memory
        masterKey = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        setContentView(R.layout.activity_registration);
        final TextView key = (TextView) findViewById(R.id.master_key);
        final TextView keyVetify = (TextView) findViewById(R.id.master_key_verify);
        final TextView login = (TextView) findViewById(R.id.login);
        final TextView pass = (TextView) findViewById(R.id.password);
        Button btnRegister = (Button) findViewById(R.id.btnRegister);
        Button btnToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String loginText = login.getText().toString();
                String passText = pass.getText().toString();


                //start save master key
                String keyText = key.getText().toString();
                String keyTextVerify = keyVetify.getText().toString();
                if((key.length() >=  MIN_KEY_LENGHT ) && (!key.equals(""))  && (keyTextVerify.equals(keyText)) ) {
                    SharedPreferences.Editor e = masterKey.edit();
                    e.putString(APP_PREFERENCES_KEY, keyTextVerify);
                    e.commit();
                    Intent intent = new Intent(Registration.this, Lock.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

                //end save master key

            }
        });

        btnToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Registration.this, Login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

    }
}
