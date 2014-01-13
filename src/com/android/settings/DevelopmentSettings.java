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

package com.android.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Usb;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.CheckBoxPreference;
import android.provider.Settings;
import android.text.TextUtils;

/*
 * Displays preferences for application developers.
 */
public class DevelopmentSettings extends PreferenceActivity
        implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener {

    private static final String ENABLE_ADB = "enable_adb";
    private static final String KEEP_SCREEN_ON = "keep_screen_on";
    private static final String ALLOW_MOCK_LOCATION = "allow_mock_location";

    private MyCheckBoxPreference mEnableAdb;
    private MyCheckBoxPreference mKeepScreenOn;
    private MyCheckBoxPreference mAllowMockLocation;

    // To track whether Yes was clicked in the adb warning dialog
    private boolean mOkClicked;

    private Dialog mOkDialog;

    private BroadcastReceiver mUsbStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Usb.ACTION_USB_STATE)) {
                handleUsbStateChanged(intent);
            }
        }
    };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.development_prefs);

        mEnableAdb = (MyCheckBoxPreference) findPreference(ENABLE_ADB);
        mKeepScreenOn = (MyCheckBoxPreference) findPreference(KEEP_SCREEN_ON);
        mAllowMockLocation = (MyCheckBoxPreference) findPreference(ALLOW_MOCK_LOCATION);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_STATUSBAR_USB);
        filter.addAction(Usb.ACTION_USB_STATE);
        registerReceiver(mUsbStateReceiver, filter);

        mEnableAdb.setChecked(Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ADB_ENABLED, 0) != 0);
        mKeepScreenOn.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.STAY_ON_WHILE_PLUGGED_IN, 0) != 0);
        mAllowMockLocation.setChecked(Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (Utils.isMonkeyRunning()) {
            return false;
        }

        if (preference == mEnableAdb) {
            if (mEnableAdb.isChecked()) {
                mOkClicked = false;
                if (mOkDialog != null) dismissDialog();
                mOkDialog = new AlertDialog.Builder(this).setMessage(
                        getResources().getString(R.string.adb_warning_message))
                        .setTitle(R.string.adb_warning_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, this)
                        .setNegativeButton(android.R.string.no, this)
                        .show();
                mOkDialog.setOnDismissListener(this);
            } else {
                Settings.Secure.putInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 0);
            }
        } else if (preference == mKeepScreenOn) {
            Settings.System.putInt(getContentResolver(), Settings.System.STAY_ON_WHILE_PLUGGED_IN, 
                    mKeepScreenOn.isChecked() ? 
                    (BatteryManager.BATTERY_PLUGGED_AC | BatteryManager.BATTERY_PLUGGED_USB) : 0);
        } else if (preference == mAllowMockLocation) {
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION,
                    mAllowMockLocation.isChecked() ? 1 : 0);
        }

        return false;
    }

    private void dismissDialog() {
        if (mOkDialog == null) return;
        mOkDialog.dismiss();
        mOkDialog = null;
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            mOkClicked = true;
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 1);
        } else {
            // Reset the toggle
            mEnableAdb.setChecked(false);
        }
    }

    public void onDismiss(DialogInterface dialog) {
        // Assuming that onClick gets called first
        if (!mOkClicked) {
            mEnableAdb.setChecked(false);
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mUsbStateReceiver);
        dismissDialog();
        super.onDestroy();
    }

    private void handleUsbStateChanged(Intent intent) {
        boolean connected = intent.getExtras().getBoolean(Usb.USB_CONNECTED);
        if (connected) {
            mEnableAdb.setEnabled(false);
        } else {
            mEnableAdb.setEnabled(true);
        }
    }
}
