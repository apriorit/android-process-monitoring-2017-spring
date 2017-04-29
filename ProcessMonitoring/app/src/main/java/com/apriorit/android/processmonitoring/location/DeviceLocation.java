package com.apriorit.android.processmonitoring.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;


public class DeviceLocation implements LocationListener {
    private Context mContext;
    private double mLatitude;
    private double mLongtitude;

    public DeviceLocation(Context context) {
        mContext = context;

        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 3, 0, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongtitude() {
        return mLongtitude;
    }

    @Override
    public void onLocationChanged(Location location) {
        mLatitude = location.getLatitude();
        mLongtitude = location.getLongitude();
        //Toast.makeText(mContext, "Network: " + Double.toString(mLatitude) + " " + Double.toString(mLongtitude), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {

        } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {

        }
    }
}
