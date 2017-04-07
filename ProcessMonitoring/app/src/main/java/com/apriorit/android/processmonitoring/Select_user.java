package com.apriorit.android.processmonitoring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apriorit.android.processmonitoring.request_handler.Handler;
import com.apriorit.android.processmonitoring.request_handler.JsonHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.apriorit.android.processmonitoring.R.layout.*;


public class Select_user extends AppCompatActivity {
    LinearLayout llt;
    private Handler requestHander;
    private BroadcastReceiver broadcastReceiver;
    private static SharedPreferences accountName;

    public static final String APP_PREFERENCES = "preference";
    public static final String APP_PREFERENCES_ACCOUNT_NAME = "accountName";
    private static String accountNameText;
    private static HashSet<String> accountUsers = new HashSet<String>();
    private static TextView massage;

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
        setContentView(activity_select_user);
        //hide massage
        massage = (TextView) findViewById(R.id.massage);

        massage.setVisibility(View.GONE);
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
        //get and Print Users
        accountNameText = accountName.getString(APP_PREFERENCES_ACCOUNT_NAME, "");
        getUsers(accountNameText);
        //button get users
//        Button btn =  new Button(this);
//        btn.setLayoutParams(lButtonParams);
//        btn.setId(0);
//        btn.setText("Get users");
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                accountNameText = accountName.getString(APP_PREFERENCES_ACCOUNT_NAME, "");
//                getUsers(accountNameText);
//            }
//        });

//        llt.addView(btn);
        //button create user
        Button btn =  new Button(this);
        btn.setLayoutParams(lButtonParams);
        btn.setId(-1);
        btn.setText("Create user");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDCreaterUser();
            }
        });
        llt.addView(btn);
    }
    //create dialog GET USER
    private void showDCreaterUser(){

        LayoutInflater inflater = getLayoutInflater();

        final View layout = inflater.inflate(R.layout.divalog_create_user,
                (ViewGroup)findViewById(R.id.createUser));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EditText name = (EditText) layout.findViewById(R.id.userName);
                String userName = String.valueOf(name.getText());
                if(accountUsers.contains(userName)){
                    massage.setText("A user with this name exists");
                    massage.setVisibility(View.VISIBLE);
                }
                else{
                    setToken("","");
                }

            }
        }).create();
        builder.setView(layout);
        builder.show();
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
                accountUsers.add((String) entry.getValue());
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
