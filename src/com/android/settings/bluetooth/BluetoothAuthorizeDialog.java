/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.bluetooth;

import com.android.internal.app.AlertController;
import com.android.internal.app.AlertActivity;
import com.android.settings.R;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothUuid;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class BluetoothAuthorizeDialog extends AlertActivity
        implements DialogInterface.OnClickListener {
    private static final String TAG = "BluetoothAuthorizeDialog";
    private static final boolean V = true;

    private BluetoothDevice mDevice;
    private ListenForPairingCancel mBrcvr;
    private LocalBluetoothManager mLocalManager;
    private ParcelUuid mServiceUuid;
    private String mName;
    private boolean mTemporaryKey = false;

    private static Context mContext;
    private static PowerManager pm;
    private static PowerManager.WakeLock wl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");

        Intent intent = getIntent();
        String action = intent.getAction();

        if (!action.equals(BluetoothDevice.ACTION_AUTHORIZE_REQUEST)) {
            Log.e(TAG, "onCreate: Unknown intent " + action);
            finish();
            return;
        }

        mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        mServiceUuid = ParcelUuid.fromString(intent.getStringExtra(BluetoothDevice.EXTRA_UUID));

        mBrcvr = new ListenForPairingCancel();
        IntentFilter intFltr = new android.content.IntentFilter();
        intFltr.addAction(BluetoothDevice.ACTION_PAIRING_CANCEL);
        intFltr.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        getBaseContext().registerReceiver(mBrcvr, intFltr);

        mLocalManager = LocalBluetoothManager.getInstance(this);
        mContext = mLocalManager.getContext();
        mName = mLocalManager.getCachedDeviceManager().getName(mDevice);

        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                            PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.ON_AFTER_RELEASE, TAG);

        String svc = getServiceName(mServiceUuid);
        AlertController.AlertParams p = mAlertParams;
        p.mIconId = android.R.drawable.ic_dialog_info;
        p.mTitle = getString(R.string.authdlg_title, mName);
        p.mView = createView(mName, svc);
        p.mPositiveButtonText = getString(R.string.authdlg_service_accept);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(R.string.authdlg_service_decline);
        p.mNegativeButtonListener = this;
        setupAlert();

        boolean isScreenOn = pm.isScreenOn();
        if (!isScreenOn) {
            wl.acquire();
            Log.v(TAG, "Wake Lock acquired");
        }
    }

    private View createView(String deviceName, String svc) {
        Log.v(TAG, "createView");
        View view = getLayoutInflater().inflate(R.layout.bluetooth_authorize_service, null);
        TextView msgView = (TextView) view.findViewById(R.id.message1);
        msgView.setText(getString(R.string.authdlg_message, deviceName, svc));
        if (mTemporaryKey) {
            CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox1);
            TextView textview = (TextView) view.findViewById(R.id.autoaccept_text);
            checkbox.setEnabled(false);
            textview.setEnabled(false);
        }
        return view;
    }

    private boolean isAutoReply() {
        if (mTemporaryKey) return false;
        CheckBox checkbox = (CheckBox) findViewById(R.id.checkbox1);
        boolean isChecked = checkbox.isChecked();
        Log.v(TAG, "isChecked =" + isChecked);
        return isChecked;
    }

    private void onAuthorize() {
        Log.v(TAG, "onAuthorize");
        mDevice.authorizeService(mServiceUuid, true, isAutoReply());
    }

    private void onDecline() {
        Log.v(TAG, "onDecline");
        mDevice.authorizeService(mServiceUuid, false, isAutoReply());
    }

    public void onClick(DialogInterface dialog, int which) {
        Log.v(TAG, "onClick");
        switch (which) {
        case BUTTON_POSITIVE:
            if (wl.isHeld()) {
                wl.release();
                Log.v(TAG, "Wake Lock released");
            }
            onAuthorize();
            break;
        case BUTTON_NEGATIVE:
            if (wl.isHeld()) {
                wl.release();
                Log.v(TAG, "Wake Lock released");
            }
            onDecline();
            break;
        }
    }

    @Override
    protected void onDestroy() {
        getBaseContext().unregisterReceiver(mBrcvr);
        super.onDestroy();
    }

    private void quitActivity() {
        if (wl.isHeld()) {
            wl.release();
            Log.v(TAG, "Wake Lock released");
        }
        finish();
    }

    private String getServiceName(ParcelUuid uuid) {
        if (BluetoothUuid.isPbap(uuid))
            return getString(R.string.authdlg_service_pbap);
        if (BluetoothUuid.isOpp(uuid))
            return getString(R.string.authdlg_service_opp);
        return getString(R.string.authdlg_service_default);
    }

    private class ListenForPairingCancel extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())
             || BluetoothDevice.ACTION_PAIRING_CANCEL.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device == null || device.equals(mDevice)) {
                    String name = mName;
                    if (name == null) {
                        mContext.getString(R.string.bluetooth_remote_device);
                    }
                    Utils.showError(mContext, name, R.string.authorizaion_failed_error);
                    quitActivity();
                }
            } else {
                quitActivity();
            }
        }
    }
}
