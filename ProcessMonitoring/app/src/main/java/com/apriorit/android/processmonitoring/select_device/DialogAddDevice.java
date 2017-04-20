package com.apriorit.android.processmonitoring.select_device;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

import com.apriorit.android.processmonitoring.R;
import com.apriorit.android.processmonitoring.request_handler.Handler;

public class DialogAddDevice extends DialogFragment implements OnClickListener {
    private EditText mDeviceName;
    private String mLogin;
    private Handler mRequestHandler;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle("Enter device name");
        mLogin = getArguments().getString("login");
        mRequestHandler = new Handler(getActivity());

        View v = inflater.inflate(R.layout.dialog_add_device, null);
        v.findViewById(R.id.btnAddDevice).setOnClickListener(this);
        mDeviceName = (EditText) v.findViewById(R.id.editTextDeviceName);
        return v;
    }

    public void onClick(View v) {
        Bundle registrationData = new Bundle();
        registrationData.putString("requestType", "device_registration");
        registrationData.putString("login", mLogin);
        registrationData.putString("user_name", mDeviceName.getText().toString());
        mRequestHandler.SendDataToServer(registrationData);

        dismiss();
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }
}