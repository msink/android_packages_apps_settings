/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import com.android.settings.bluetooth.BluetoothEnabler;
import com.android.settings.wifi.WifiEnabler;
import com.android.settings.nfc.NfcEnabler;

public class WirelessSettings extends PreferenceActivity {

    private static final String KEY_TOGGLE_WIFI = "toggle_wifi";
    private static final String KEY_WIFI_SETTINGS = "wifi_settings";

    public static final String EXIT_ECM_RESULT = "exit_ecm_result";
    public static final int REQUEST_CODE_EXIT_ECM = 1;

    private WifiEnabler mWifiEnabler;

    /**
     * Invoked on each preference click in this hierarchy, overrides
     * PreferenceActivity's implementation.  Used to make sure we track the
     * preference click events.
     */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // Let the intents be launched by the Preference manager
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.wireless_settings);

        CheckBoxPreference wifi = (CheckBoxPreference) findPreference(KEY_TOGGLE_WIFI);

        mWifiEnabler = new WifiEnabler(this, wifi);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mWifiEnabler.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mWifiEnabler.pause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EXIT_ECM) {
            Boolean isChoiceYes = data.getBooleanExtra(EXIT_ECM_RESULT, false);
        }
    }
}
