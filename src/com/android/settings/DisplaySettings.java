/*
 * Copyright (C) 2010 The Android Open Source Project
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

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;
import static android.provider.Settings.System.WAKE_UP_BRIGHTNESS;

import java.util.ArrayList;

import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.IWindowManager;
import android.hardware.DeviceController;

public class DisplaySettings extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "DisplaySettings";

    /** If there is no setting in the provider, use this. */
    private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 30000;

    private static final String KEY_SCREEN_TIMEOUT = "screen_timeout";
    private static final String KEY_WAKE_UP_BRIGHTNESS = "wake_up_brightness";
    private int mBrightnessValue = 0;
    private CheckBoxPreference mWakeUpBrightness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver resolver = getContentResolver();

        DeviceController deviceController = new DeviceController(this);
        if (deviceController.hasFrontLight()) {
            addPreferencesFromResource(R.xml.display_settings);
        } else {
            addPreferencesFromResource(R.xml.display_settings_not_front_light);
        }

        ListPreference screenTimeoutPreference =
            (ListPreference) findPreference(KEY_SCREEN_TIMEOUT);
        screenTimeoutPreference.setValue(String.valueOf(Settings.System.getInt(
                resolver, SCREEN_OFF_TIMEOUT, FALLBACK_SCREEN_TIMEOUT_VALUE)));
        screenTimeoutPreference.setOnPreferenceChangeListener(this);
        disableUnusableTimeouts(screenTimeoutPreference);

        if (deviceController.hasFrontLight()) {
            mWakeUpBrightness =
                (CheckBoxPreference)findPreference(KEY_WAKE_UP_BRIGHTNESS);
            if (mWakeUpBrightness != null) {
                mWakeUpBrightness.setOnPreferenceChangeListener(this);
            }
            try {
                mBrightnessValue = Settings.System.getInt(resolver, WAKE_UP_BRIGHTNESS);
            } catch (Settings.SettingNotFoundException e) {
            }
            if (mBrightnessValue == 1) {
                mWakeUpBrightness.setChecked(true);
            } else {
                mWakeUpBrightness.setChecked(false);
            }
            Preference brightness_dialog = findPreference("brightness_dialog");
            if (brightness_dialog != null) {
                brightness_dialog.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        BrightnessDialog dialog = new BrightnessDialog(preference.getContext());
                        dialog.show();
                        return true;
                    }
                });
            }
        }
    }

    private void disableUnusableTimeouts(ListPreference screenTimeoutPreference) {
        final DevicePolicyManager dpm =
            (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        final long maxTimeout = dpm != null ? dpm.getMaximumTimeToLock(null) : 0;
        if (maxTimeout == 0) {
            return; // policy not enforced
        }
        final CharSequence[] entries = screenTimeoutPreference.getEntries();
        final CharSequence[] values = screenTimeoutPreference.getEntryValues();
        ArrayList<CharSequence> revisedEntries = new ArrayList<CharSequence>();
        ArrayList<CharSequence> revisedValues = new ArrayList<CharSequence>();
        for (int i = 0; i < values.length; i++) {
            long timeout = Long.valueOf(values[i].toString());
            if (timeout <= maxTimeout) {
                revisedEntries.add(entries[i]);
                revisedValues.add(values[i]);
            }
        }
        if (revisedEntries.size() != entries.length || revisedValues.size() != values.length) {
            screenTimeoutPreference.setEntries(
                    revisedEntries.toArray(new CharSequence[revisedEntries.size()]));
            screenTimeoutPreference.setEntryValues(
                    revisedValues.toArray(new CharSequence[revisedValues.size()]));
            final int userPreference = Integer.valueOf(screenTimeoutPreference.getValue());
            if (userPreference <= maxTimeout) {
                screenTimeoutPreference.setValue(String.valueOf(userPreference));
            } else {
                // There will be no highlighted selection since nothing in the list matches
                // maxTimeout. The user can still select anything less than maxTimeout.
                // TODO: maybe append maxTimeout to the list and mark selected.
            }
        }
        screenTimeoutPreference.setEnabled(revisedEntries.size() > 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (KEY_SCREEN_TIMEOUT.equals(key)) {
            int value = Integer.parseInt((String) objValue);
            try {
                Settings.System.putInt(getContentResolver(),
                        SCREEN_OFF_TIMEOUT, value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist screen timeout setting", e);
            }
        } else if (KEY_WAKE_UP_BRIGHTNESS.equals(key)) {
            try {
                if (!mWakeUpBrightness.isChecked()) {
                    Settings.System.putInt(getContentResolver(),
                        WAKE_UP_BRIGHTNESS, 1);
                } else {
                    Settings.System.putInt(getContentResolver(),
                        WAKE_UP_BRIGHTNESS, 0);
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist wake up brightness", e);
            }
        }

        return true;
    }

    protected void onDestroy() {
        super.onDestroy();
    }
}
