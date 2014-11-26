package com.android.settings;

import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.Preference;
import android.provider.Settings;
import android.util.Log;

public class NetworkInactivityTimeoutSettings extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "NetworkInactivityTimeoutSettings";

    private ListPreference defaultTimeout = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.network_inactivity_timeout_settings);
        initData();
    }

    private void initData() {
        ContentResolver resolver = getContentResolver();
        defaultTimeout = (ListPreference) findPreference("network_inactivity_timeout_default");
        defaultTimeout.setOnPreferenceChangeListener(this);
        int valueDefault = Settings.System.getInt(resolver, Settings.System.WIFI_INACTIVITY_TIMEOUT, 0);
        setDefaultSettingSummary(valueDefault);
        defaultTimeout.setValue(String.valueOf(valueDefault));
    }

    private String toStringValue(int value) {
        return String.valueOf((value / 1000) / 60);
    }

    private void setDefaultSettingSummary(int value) {
        if (value > 0) {
            defaultTimeout.setSummary(
                getString(R.string.network_inactivity_default_summary,
                toStringValue(value)));
        } else {
            defaultTimeout.setSummary("");
        }
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        String key = preference.getKey();
        int value = Integer.parseInt((String)objValue);
        if (key.equals("network_inactivity_timeout_default")) {
            commitChangeValues("wifi_inactivity_timeout", value);
            setDefaultSettingSummary(value);
        }
        return true;
    }

    private void commitChangeValues(String key, int value) {
        try {
            Settings.System.putInt(getContentResolver(), key, value);
        } catch (NumberFormatException e) {
            Log.e(TAG, "could not persist network inactivity timeout setting", e);
        }
    }
}
