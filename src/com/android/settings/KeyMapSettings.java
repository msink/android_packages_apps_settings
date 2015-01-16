package com.android.settings;

import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;
import android.util.Log;

public class KeyMapSettings extends PreferenceActivity {
    private InputMethodManager IMService;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.DEVICE.contentEquals("I63MLP_HD")) {
            addPreferencesFromResource(R.xml.new_key_map_settings);
        } else if (Build.DEVICE.contentEquals("C63SM") ||
                   Build.DEVICE.contentEquals("C65S") ||
                   Build.DEVICE.contentEquals("C65S_GRAMMATA") ||
                   Build.DEVICE.contentEquals("C65S_DATASOFT") ||
                   Build.DEVICE.contentEquals("C65S_ARTATECH")) {
            addPreferencesFromResource(R.xml.long_short_click_key_map_settings);
        } else {
            addPreferencesFromResource(R.xml.key_map_settings);
        }
        SummaryListPreference key_map_mode_list =
            (SummaryListPreference)findPreference("key_map_mode");
        key_map_mode_list.setValue(String.valueOf(Settings.System.getInt(
            getContentResolver(), "key_map_mode", 1)));
        IMService = (InputMethodManager)getSystemService("input_method");
        key_map_mode_list.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int value = Integer.parseInt((String)newValue);
                try {
                    Settings.System.putInt(getContentResolver(), "key_map_mode", value);
                    IMService.setKeyMapMode(value);
                } catch (NumberFormatException e) {
                     Log.e("KeyMapSettings", "could not persist Key Map setting", e);
                }
                return true;
            }
        });
    }
}