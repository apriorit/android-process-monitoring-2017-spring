package com.apriorit.android.processmonitoring.select_device;

class DeviceModel {
    private String mDeviceName;
    private int mUserID;

    DeviceModel(String name, int id) {
        mDeviceName = name;
        mUserID = id;
    }

    String getDeviceName() {
        return mDeviceName;
    }

    int getUserID() {
        return mUserID;
    }
}
