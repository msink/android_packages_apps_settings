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

    private static final String KEY_TOGGLE_AIRPLANE = "toggle_airplane";
    private static final String KEY_TOGGLE_BLUETOOTH = "toggle_bluetooth";
    private static final String KEY_TOGGLE_WIFI = "toggle_wifi";
    private static final String KEY_TOGGLE_NFC = "toggle_nfc";
    private static final String KEY_WIFI_SETTINGS = "wifi_settings";
    private static final String KEY_BT_SETTINGS = "bt_settings";
    private static final String KEY_VPN_SETTINGS = "vpn_settings";
    private static final String KEY_TETHER_SETTINGS = "tether_settings";

    public static final String EXIT_ECM_RESULT = "exit_ecm_result";
    public static final int REQUEST_CODE_EXIT_ECM = 1;

    private WifiEnabler mWifiEnabler;
    private NfcEnabler mNfcEnabler;

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

    public static boolean isRadioAllowed(Context context, String type) {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.wireless_settings);

        CheckBoxPreference wifi = (CheckBoxPreference) findPreference(KEY_TOGGLE_WIFI);
        CheckBoxPreference nfc = (CheckBoxPreference) findPreference(KEY_TOGGLE_NFC);
        Preference vpn = findPreference(KEY_VPN_SETTINGS);

        mWifiEnabler = new WifiEnabler(this, wifi);
        mNfcEnabler = new NfcEnabler(this, nfc);

        String toggleable = Settings.System.getString(getContentResolver(),
                Settings.System.AIRPLANE_MODE_TOGGLEABLE_RADIOS);

        // Remove NFC if its not available
        if (NfcAdapter.getDefaultAdapter() == null) {
            getPreferenceScreen().removePreference(nfc);
        }

        if (!SystemProperties.getBoolean("ro.service.vpn.enabled", false)) {
            getPreferenceScreen().removePreference(vpn);
        }

        // Disable Tethering if it's not allowed
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!cm.isTetheringSupported()) {
        } else {
            String[] usbRegexs = cm.getTetherableUsbRegexs();
            String[] wifiRegexs = cm.getTetherableWifiRegexs();
            Preference p = findPreference(KEY_TETHER_SETTINGS);
            if (wifiRegexs.length == 0) {
                p.setTitle(R.string.tether_settings_title_usb);
                p.setSummary(R.string.tether_settings_summary_usb);
            } else {
                if (usbRegexs.length == 0) {
                    p.setTitle(R.string.tether_settings_title_wifi);
                    p.setSummary(R.string.tether_settings_summary_wifi);
                } else {
                    p.setTitle(R.string.tether_settings_title_both);
                    p.setSummary(R.string.tether_settings_summary_both);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mWifiEnabler.resume();
        mNfcEnabler.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mWifiEnabler.pause();
        mNfcEnabler.pause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EXIT_ECM) {
        }
    }
}
