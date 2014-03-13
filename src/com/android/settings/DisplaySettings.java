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

import static android.provider.Settings.System.EPD_REFRESH_MODE;
import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;
import static android.provider.Settings.System.AUTO_SHUTDOWN_TIMEOUT;

import java.util.ArrayList;

import android.app.ActivityManagerNative;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
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

public class DisplaySettings extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "DisplaySettings";

    /** If there is no setting in the provider, use this. */
    private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 30000;

    private static final String KEY_PAGE_REFRESH_RATE = "page_refresh_rate";
    private static final String KEY_SCREEN_TIMEOUT = "screen_timeout";
    private static final String KEY_AUTO_SHUTDOWN_TIMEOUT = "auto_shutdown_timeout";
    private static final String KEY_FONT_SIZE = "font_size";

    private IWindowManager mWindowManager;

    private final Configuration mCurConfig = new Configuration();
    private ListPreference mFontSizePref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver resolver = getContentResolver();
        mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));

        addPreferencesFromResource(R.xml.display_settings);

        mFontSizePref = (ListPreference) findPreference(KEY_FONT_SIZE);
        mFontSizePref.setOnPreferenceChangeListener(this);

        ListPreference refresh =
            (ListPreference) findPreference(KEY_PAGE_REFRESH_RATE);
        refresh.setValue(String.valueOf
            (Settings.System.getInt(resolver, EPD_REFRESH_MODE, -1)));
        refresh.setOnPreferenceChangeListener(this);

        ListPreference sleep =
            (ListPreference) findPreference(KEY_SCREEN_TIMEOUT);
        sleep.setValue(String.valueOf
            (Settings.System.getInt(resolver, SCREEN_OFF_TIMEOUT, FALLBACK_SCREEN_TIMEOUT_VALUE)));
        sleep.setOnPreferenceChangeListener(this);

        ListPreference shutdown =
            (ListPreference) findPreference(KEY_AUTO_SHUTDOWN_TIMEOUT);
        shutdown.setValue(String.valueOf
            (Settings.System.getInt(resolver, AUTO_SHUTDOWN_TIMEOUT, -1)));
        shutdown.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();

        if (KEY_PAGE_REFRESH_RATE.equals(key)) {
            int value = Integer.parseInt((String) objValue);
            try {
                Settings.System.putInt(getContentResolver(), EPD_REFRESH_MODE, value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist epd refresh setting", e);
            }

        } else if (KEY_SCREEN_TIMEOUT.equals(key)) {
            int value = Integer.parseInt((String) objValue);
            try {
                Settings.System.putInt(getContentResolver(), SCREEN_OFF_TIMEOUT, value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist sleep timeout setting", e);
            }

        } else if (KEY_AUTO_SHUTDOWN_TIMEOUT.equals(key)) {
            int value = Integer.parseInt((String) objValue);
            try {
                Settings.System.putInt(getContentResolver(), AUTO_SHUTDOWN_TIMEOUT, value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist auto shutdown setting", e);
            }

        } else if (KEY_FONT_SIZE.equals(key)) {
            writeFontSizePreference(objValue);
        }

        return true;
    }

    public void writeFontSizePreference(Object objValue) {
        try {
            mCurConfig.fontScale = Float.parseFloat(objValue.toString());
            ActivityManagerNative.getDefault().updateConfiguration(mCurConfig);
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to save font size");
        }
    }

    public void readFontSizePreference(ListPreference pref) {
        try {
            mCurConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to retrieve font size");
        }

        // mark the appropriate item in the preferences list
        int index = floatToIndex(mCurConfig.fontScale, R.array.entryvalues_font_size);
        pref.setValueIndex(index);

        // report the current size in the summary text
        final Resources res = getResources();
        String[] fontSizeNames = res.getStringArray(R.array.entries_font_size);
        pref.setSummary(String.format(res.getString(R.string.summary_font_size),
                fontSizeNames[index]));
    }

    int floatToIndex(float val, int resid) {
        String[] indices = getResources().getStringArray(resid);
        float lastVal = Float.parseFloat(indices[0]);
        for (int i=1; i<indices.length; i++) {
            float thisVal = Float.parseFloat(indices[i]);
            if (val < (lastVal + (thisVal-lastVal)*.5f)) {
                return i-1;
            }
            lastVal = thisVal;
        }
        return indices.length-1;
    }

    @Override
    public void onResume() {
        super.onResume();

        updateState();
    }

    private void updateState() {
        readFontSizePreference(mFontSizePref);
    }
}
