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

import com.android.settings.R;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.util.Log;

public class BluetoothAuthorizeRequest extends BroadcastReceiver {
    private static final String TAG = "BluetoothAuthorizeRequest";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive");
        String action = intent.getAction();
        if (action.equals(BluetoothDevice.ACTION_AUTHORIZE_REQUEST)) {
            BluetoothDevice device = (BluetoothDevice)
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            ParcelUuid service = ParcelUuid.fromString(
                    intent.getStringExtra(BluetoothDevice.EXTRA_UUID));
            intent.setClass(context, BluetoothAuthorizeDialog.class);
            intent.setAction(BluetoothDevice.ACTION_AUTHORIZE_REQUEST);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            Log.e(TAG, "Unknown intent action:  " + action);
        }
        context.startActivity(intent);
    }
}
