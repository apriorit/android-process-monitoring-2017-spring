package com.apriorit.android.processmonitoring.device_management;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.apriorit.android.processmonitoring.R;

import java.util.List;

/**
 * Custom adapter for list view
 */
class AppsListViewAdapter extends BaseAdapter {
    private LayoutInflater mlInflater;
    private List<AppDataModel> mListAppsDataModel;

    AppsListViewAdapter(Context context, List<AppDataModel> apps) {
        mListAppsDataModel = apps;
        mlInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // Returns count of elements
    @Override
    public int getCount() {
        return mListAppsDataModel.size();
    }

    // Returns element by index
    @Override
    public Object getItem(int position) {
        return mListAppsDataModel.get(position);
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
            view = mlInflater.inflate(R.layout.item_application, parent, false);
        }
        TextView nameApp = (TextView) view.findViewById(R.id.textViewAppName);
        final Switch switchAppAccess = (Switch) view.findViewById(R.id.switchAccess);

        //Get the data model for this position
        AppDataModel p = getAppModel(position);
        nameApp.setText(p.getAppName());

        //Handles click on switch
        switchAppAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchAppAccess.isChecked()) {
                    setAccess(position, true);
                } else {
                    setAccess(position, false);
                }
            }
        });
        return view;
    }

    private AppDataModel getAppModel(int position) {
        return ((AppDataModel) getItem(position));
    }

    /**
     * Adds or deletes application from Blacklist
     */
    private void setAccess(int position, Boolean access) {
        ((AppDataModel) getItem(position)).setAccess(access);
    }

    /**
     *  Correct displaying items while scrolling
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