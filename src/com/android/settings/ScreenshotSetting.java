package com.android.settings;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.util.Log;

public class ScreenshotSetting extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String KEY_SCREENSHOT_DELAY = "screenshot_delay";
    private static final String KEY_SCREENSHOT_STORAGE_LOCATION = "screenshot_storage";
    private static final String KEY_SCREENSHOT_SHOW = "screenshot_show";
    private static final String KEY_SCREENSHOT_VERSION = "screenshot_version";

    private Context mContext;
    private ListPreference mDelay;
    private android.content.IntentFilter filter;
    private SharedPreferences.Editor mEdit;
    private Screenshot mScreenshot;
    private SharedPreferences mSharedPreference;
    private ListPreference mStorage;
    private Preference mVersion;
    private CheckBoxPreference mShow;
    private Dialog dialog;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            mDelay.setValue(null);
            mDelay.setSummary(null);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.screenshot);

        mContext = getActivity();
        mDelay = (ListPreference)findPreference(KEY_SCREENSHOT_DELAY);
        mStorage = (ListPreference)findPreference(KEY_SCREENSHOT_STORAGE_LOCATION);
        mVersion = (Preference)findPreference(KEY_SCREENSHOT_VERSION);

        mDelay.setOnPreferenceChangeListener(this);
        mStorage.setOnPreferenceChangeListener(this);

        mSharedPreference = getPreferenceScreen().getSharedPreferences();
        mEdit = mSharedPreference.edit();

        String summary_storage = mStorage.getSharedPreferences()
            .getString(KEY_SCREENSHOT_STORAGE_LOCATION, "flash");
        mStorage.setValue(summary_storage);
        mStorage.setSummary(summary_storage);

        getPreferenceScreen().removePreference(mVersion);

        filter = new IntentFilter("rk.android.screenshot.ACTION");
        mScreenshot = (Screenshot) getActivity().getApplication();
    }

    public void onPause() {
        super.onPause();
        mContext.unregisterReceiver(receiver);
    }

    public void onResume() {
        super.onResume();
        mDelay.setSummary(null);
        mDelay.setValue(null);
        mContext.registerReceiver(receiver, filter);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mDelay) {
            int value = Integer.parseInt((String)newValue);
            mDelay.setSummary((String)newValue + getString(R.string.later));
            mScreenshot.startScreenshot(value);

        } else if (preference == mStorage) {
            String value = (String)newValue;
            if(value.equals("flash")) {
                Settings.System.putString(getContentResolver(),
                    Settings.System.SCREENSHOT_LOCATION, "/mnt/sdcard");
            } else if(value.equals("sdcard")) {
                    Settings.System.putString(getContentResolver(),
                    Settings.System.SCREENSHOT_LOCATION, "/mnt/external_sd");
            } else if(value.equals("usb")) {
                    Settings.System.putString(getContentResolver(),
                    Settings.System.SCREENSHOT_LOCATION, "/mnt/usb_storage");
            }
            mStorage.setSummary(value);
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        return true;
    }
}
