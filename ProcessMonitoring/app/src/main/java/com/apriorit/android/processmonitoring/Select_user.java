package com.apriorit.android.processmonitoring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.apriorit.android.processmonitoring.request_handler.Handler;
import com.apriorit.android.processmonitoring.request_handler.JsonHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class Select_user extends AppCompatActivity {
    LinearLayout llt;
    private Handler requestHander;
    private BroadcastReceiver broadcastReceiver;
    private static SharedPreferences accountName;

    public static final String APP_PREFERENCES = "preference";
    public static final String APP_PREFERENCES_ACCOUNT_NAME = "accountName";
    private static String accountNameText;
    @Override
    protected void onResume(){
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter("USERS"));
        Log.d("MY","Enter ONresume");
    }
    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        Log.d("MY","Enter ONpause");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // get account Name
        accountName = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);
        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                printUsers(intent);
            }
        };
        requestHander = new Handler(this);
        llt = (LinearLayout) findViewById(R.id.activity_select_user);
        //layout params for every Button
        LinearLayout.LayoutParams lButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,  LinearLayout.LayoutParams.WRAP_CONTENT);
        Button btn =  new Button(this);
        btn.setLayoutParams(lButtonParams);
        btn.setId(0);
        btn.setText("Get users");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accountNameText = accountName.getString(APP_PREFERENCES_ACCOUNT_NAME, "");
                getUsers(accountNameText);
            }
        });

        llt.addView(btn);
    }
    private void getUsers(String accName) {
        Bundle registrationData = new Bundle();
        registrationData.putString("requestType", "getUsers");
        registrationData.putString("accName", accName);
        requestHander.SendDataToServer(registrationData);
    }

    private void printUsers(Intent intent){
        LinearLayout.LayoutParams lButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,  LinearLayout.LayoutParams.WRAP_CONTENT);
        try {
            JSONObject jsonObj = new JSONObject(intent.getStringExtra("list"));
            Map<String, Object> users = JsonHelper.toMap(jsonObj);
            for(final Map.Entry<String, Object> entry: users.entrySet()){
                Button btn =  new Button(this);
                btn.setLayoutParams(lButtonParams);
                btn.setId(Integer.parseInt(entry.getKey()));
                btn.setText((String) entry.getValue());
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setToken((String) entry.getValue(),"1");
                    }
                });
                llt.addView(btn);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void setToken(String userName,String tokenId){
        Bundle registrationData = new Bundle();
        registrationData.putString("requestType", "setToken");
        registrationData.putString("userName", userName);
        registrationData.putString("tokenId", tokenId);
        requestHander.SendDataToServer(registrationData);
    }
}
