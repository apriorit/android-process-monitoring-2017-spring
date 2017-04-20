package com.apriorit.android.processmonitoring.select_device;

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
class DeviceListViewAdapter extends BaseAdapter {
    private LayoutInflater mlInflater;
    private List<DeviceModel> mListDevices;
    private Handler mRequestHandler;

    DeviceListViewAdapter(Context context, List<DeviceModel> devices) {
        mListDevices = devices;
        mRequestHandler = new Handler(context);
        mlInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // Returns count of elements
    @Override
    public int getCount() {
        return mListDevices.size();
    }

    // Returns element by index
    @Override
    public Object getItem(int position) {
        return mListDevices.get(position);
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
            view = mlInflater.inflate(R.layout.item_device, parent, false);
        }
        TextView deviceName = (TextView) view.findViewById(R.id.device_name_text_view);
        deviceName.setText(mListDevices.get(position).getDeviceName());

        Button btnDeleteDevice = (Button) view.findViewById(R.id.delete_device_btn);
        btnDeleteDevice.setBackgroundResource(R.drawable.button_delete_icon);
        btnDeleteDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int userID = mListDevices.get(position).getUserID();
                mRequestHandler.deleteDevice(userID);
                mListDevices.remove(position);
                notifyDataSetChanged();
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
