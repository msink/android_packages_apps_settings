package com.android.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;
import android.util.Log;

public class KeyMapSettings extends PreferenceActivity {
    private static final String TAG = "KeyMapSettings";
    private InputMethodManager IMService;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.long_short_click_key_map_settings);
        SummaryListPreference key_map_mode_list =
            (SummaryListPreference) findPreference("key_map_mode");
        key_map_mode_list.setValue(String.valueOf(Settings.System.getInt(
            getContentResolver(), Settings.System.KEY_MAP_MODE, 1)));
        IMService = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        key_map_mode_list.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int value = Integer.parseInt((String)newValue);
                try {
                    Settings.System.putInt(getContentResolver(), Settings.System.KEY_MAP_MODE, value);
                    IMService.setKeyMapMode(value);
                } catch (NumberFormatException e) {
                     Log.e(TAG, "could not persist Key Map setting", e);
                }
                return true;
            }
        });
    }
}
