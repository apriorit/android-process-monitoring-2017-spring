package com.apriorit.android.processmonitoring.device_management;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.apriorit.android.processmonitoring.R;

import java.util.List;

public class LocationListViewAdapter extends BaseAdapter {
    private LayoutInflater mlInflater;
    private List<String> mListData;

    LocationListViewAdapter(Context context, List<String> data) {
        mListData = data;
        mlInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // Returns count of elements
    @Override
    public int getCount() {
        return mListData.size();
    }

    // Returns element by index
    @Override
    public Object getItem(int position) {
        return mListData.get(position);
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
            view = mlInflater.inflate(R.layout.item_location, parent, false);
        }
        TextView txtTime = (TextView) view.findViewById(R.id.txt_view_time);
        txtTime.setText(mListData.get(position));

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
