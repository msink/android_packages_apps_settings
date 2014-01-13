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

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeviceInfoSettings extends PreferenceActivity {
    private static final String TAG = "DeviceInfoSettings";

    private static final String KEY_CONTAINER = "container";
    private static final String KEY_TEAM = "team";
    private static final String KEY_CONTRIBUTORS = "contributors";
    private static final String KEY_TERMS = "terms";
    private static final String KEY_LICENSE = "license";
    private static final String KEY_COPYRIGHT = "copyright";
    private static final String KEY_SYSTEM_UPDATE_SETTINGS = "system_update_settings";
    private static final String PROPERTY_URL_SAFETYLEGAL = "ro.url.safetylegal";
    private static final String PRODUCT_VERSION = SystemProperties.get("product.version", "default");

    long[] mHits = new long[3];

    private ChildTitlePreference preferenceBackSettings;
    private MyIconPreferenceScreen preferenceStautsInfoSettings;
    private TitlePreference titlePre;

    @Override
    protected void onCreate(Bundle icicle) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.device_info_settings);

        // If we don't have an IME tutorial, remove that option
        String currentIme = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD);
        ComponentName component = ComponentName.unflattenFromString(currentIme);
        Intent imeIntent = new Intent(component.getPackageName() + ".tutorial");
        PackageManager pm = getPackageManager();
        List<ResolveInfo> tutorials = pm.queryIntentActivities(imeIntent, 0);
        if(tutorials == null || tutorials.isEmpty()) {
            getPreferenceScreen().removePreference(findPreference("system_tutorial"));
        }

        titlePre = (TitlePreference) findPreference("device_title");
        preferenceBackSettings = (ChildTitlePreference)
                findPreference("device_back");
        preferenceStautsInfoSettings = (MyIconPreferenceScreen)
                findPreference("status_info");

        String buildName = getSystemVersion();
        setStringSummary("firmware_version", Build.VERSION.RELEASE);
        findPreference("firmware_version").setEnabled(true);
        setStringSummary("device_model", "TB-176FL");
        findPreference("kernel_version").setSummary("2.6.35");
        setStringSummary("build_number", buildName);

        TelephonyManager tm = (TelephonyManager)
                getBaseContext().getSystemService("phone");

        getListView().setDividerHeight(-1);
        getListView().setDivider(null);

        // Remove Safety information preference if PROPERTY_URL_SAFETYLEGAL is not set
        removePreferenceIfPropertyMissing(getPreferenceScreen(), "safetylegal",
                PROPERTY_URL_SAFETYLEGAL);

        /*
         * Settings is a generic app and should not contain any device-specific
         * info.
         */

        // These are contained by the root preference screen
        PreferenceGroup parentPreference = getPreferenceScreen();
        Utils.updatePreferenceToSpecificActivityOrRemove(this, parentPreference,
                KEY_SYSTEM_UPDATE_SETTINGS,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == this.preferenceStautsInfoSettings) {
            finish();
        } else if (preference == this.preferenceBackSettings) {
            finish();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void removePreferenceIfPropertyMissing(PreferenceGroup preferenceGroup,
            String preference, String property ) {
        if (SystemProperties.get(property).equals(""))
        {
            // Property is missing so remove preference from group
            try {
                preferenceGroup.removePreference(findPreference(preference));
            } catch (RuntimeException e) {
                Log.d(TAG, "Property '" + property + "' missing and no '"
                        + preference + "' preference");
            }
        }
    }

    private void setStringSummary(String preference, String value) {
        try {
            findPreference(preference).setSummary(value);
        } catch (RuntimeException e) {
            findPreference(preference).setSummary(
                getResources().getString(R.string.device_info_default));
        }
    }

    private void setValueSummary(String preference, String property) {
        try {
            findPreference(preference).setSummary(
                    SystemProperties.get(property,
                            getResources().getString(R.string.device_info_default)));
        } catch (RuntimeException e) {

        }
    }

    public String getSystemVersion() {
        String version = SystemProperties.get("ro.product.version");
        if (version == null || version.length() == 0) {
            version = "1.0.0";
        }
        return version;
    }

    @Override
    public void onPause() {
        super.onPause();
        Help.stopTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Help.startTimerChangeBattery(titlePre);
    }
}
