package com.apriorit.android.processmonitoring.device_management.view_files;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.apriorit.android.processmonitoring.R;
import com.apriorit.android.processmonitoring.request_handler.Handler;

import java.util.List;

/**
 * Custom adapter for list view
 */
class FilesListViewAdapter extends BaseAdapter {
    private LayoutInflater mlInflater;
    private List<String> mListFiles;
    private Handler mRequestHandler;
    private String mCurrentDirectory;
    private String mUserID;

    FilesListViewAdapter(Context context, List<String> files, String currentDirectory, String userID) {
        mListFiles = files;
        mRequestHandler = new Handler(context);
        mCurrentDirectory = currentDirectory;
        mUserID = userID;
        mlInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // Returns count of elements
    @Override
    public int getCount() {
        return mListFiles.size();
    }

    // Returns element by index
    @Override
    public Object getItem(int position) {
        return mListFiles.get(position);
    }

    // Returns id by index
    @Override
    public long getItemId(int position) {
        return position;
    }

    // Item in list: application name and switch
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = mlInflater.inflate(R.layout.item_file, parent, false);
        }
        TextView fileNameTxtView = (TextView) view.findViewById(R.id.file_name);
        fileNameTxtView.setText(mListFiles.get(position));

        Button btnDownloadFile = (Button) view.findViewById(R.id.btn_download_file);
        btnDownloadFile.setBackgroundResource(R.drawable.button_download_icon);
        btnDownloadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestHandler.requestSendFile(mUserID, mCurrentDirectory + "/" + mListFiles.get(position), mListFiles.get(position));
            }
        });
        return view;
    }

    /**
     * Correct displaying items while scrolling
     */
    @Override
    public int getViewTypeCount() {
        return getCount();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
