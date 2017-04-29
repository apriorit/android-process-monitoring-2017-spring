package com.apriorit.android.processmonitoring.device_management.view_files;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.apriorit.android.processmonitoring.R;
import com.apriorit.android.processmonitoring.request_handler.Handler;
import com.apriorit.android.processmonitoring.request_handler.JsonHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewFilesActivity extends AppCompatActivity {
    private List<String> mListFiles;
    private ListView mListViewFiles;
    private FilesListViewAdapter mListViewAdapter;
    private String mUserID;
    private Handler mHandler;
    private String mPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_files);

        //set userID
        Intent intent = getIntent();
        mUserID = intent.getStringExtra("user-id");

        mHandler = new Handler(this);
        mListFiles = new ArrayList<>();
        mListViewFiles = (ListView) findViewById(R.id.listViewFiles);

        //request to get list with files and folders
        requestListFiles("root");
    }

    private void requestListFiles(String directory) {
        Bundle data = new Bundle();
        data.putString("requestType", "get-list-files");
        data.putString("user-id", mUserID);
        data.putString("directory", directory);
        mHandler.SendDataToServer(data);
    }

    private void showListWithFiles(Intent intent) {
        mListViewAdapter = null;
        mListFiles.clear();

        try {
            //parse json string
            JSONObject jsonObj = new JSONObject(intent.getStringExtra("files"));
            Map<String, Object> mSourceListApps = JsonHelper.toMap(jsonObj);
            for (Map.Entry<String, Object> entry : mSourceListApps.entrySet()) {
                if (entry.getKey().equals("folder")) {
                    mPath = entry.getValue().toString();
                } else {
                    mListFiles.add(entry.getValue().toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Set adapter and show list
        mListViewAdapter = new FilesListViewAdapter(this, mListFiles, mPath, mUserID);
        mListViewFiles.setAdapter(mListViewAdapter);

        mListViewFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                requestListFiles(mPath + "/" + mListFiles.get(position));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Registrate receiver which gets list with files
        registerReceiver(broadcastReceiver, new IntentFilter("LIST_FILES"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    //Receives list with app in order to display in list view
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showListWithFiles(intent);
        }
    };
}
