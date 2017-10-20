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

import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.hardware.display.WifiDisplay;
import android.hardware.display.WifiDisplayStatus;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import com.android.internal.view.RotationPolicy;
import com.android.settings.DreamSettings;

import java.util.ArrayList;
import java.util.LinkedList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

public class DisplaySettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final String TAG = "DisplaySettings";

    /** If there is no setting in the provider, use this. */
    private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 30000;

    private static final String KEY_SCREEN_TIMEOUT = "screen_timeout";
    private static final String KEY_ACCELEROMETER = "accelerometer";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_RESOLUTION = "resolution";
    private static final String DUMPSYS_DATA_PATH = "/data/system/";
    private static final String STORE_RESOLUTION_PATH = "storeresolution";
    private static final String KEY_NOTIFICATION_PULSE = "notification_pulse";
    private static final String KEY_SCREEN_SAVER = "screensaver";
    private static final String KEY_WIFI_DISPLAY = "wifi_display";
    private static final String KEY_STATUSBAR = "statusbar_set";
    private static final String KEY_VOLUME_BUTTONS = "volume_buttons_set";
    private static final String KEY_A2_FLUSH = "a2_flush_set";
    private static final String KEY_FULLSCREEN_FLUSH = "fullscreen_flush";
    private static final String KEY_OPEN_EPUB = "open_epub_setting";
    private static final String KEY_PIC_RECOVER = "pic_recover";
    private static final String KEY_SHUTDOWN_DELAY = "shutdown_delay";

    private static final int DLG_GLOBAL_CHANGE_WARNING = 1;
    private static final int DLG_CONFIRM_REBOOT = 2;

    private DisplayManager mDisplayManager;

    private CheckBoxPreference mAccelerometer;
    private WarnedListPreference mFontSizePref;
    private CheckBoxPreference mNotificationPulse;

    private final Configuration mCurConfig = new Configuration();
    
    private ListPreference mScreenTimeoutPreference;
    private Preference mScreenSaverPreference;

    private WifiDisplayStatus mWifiDisplayStatus;
    private Preference mWifiDisplayPreference;
    private ListPreference mResolutionPreference;
    private Preference mStatusbarPreference;
    private Preference mVolumeButtonsPreference;
    private Preference mA2FlushPreference;
    private ListPreference mFullscreenFlushPreference;
    private Preference mOpenEpubPreference;
    private Preference mPicrecoverPreference;
    private ListPreference mShutdownDelayPreference;

    private int mCurrentResolution = 1;
    private String mRequestResolution = "";
    private LinkedList<String> mResolutionDpis = new LinkedList();
    private LinkedList<String> mResolutionEntries = new LinkedList();
    private LinkedList<String> mResolutionEntryValues = new LinkedList();

    private boolean applyResolution() {
        File storeFile = null;
        FileOutputStream outFileStream = null;
        BufferedOutputStream buffstream = null;
        try {
            storeFile = new File(DUMPSYS_DATA_PATH+STORE_RESOLUTION_PATH+".bin");
            if (!storeFile.exists()) storeFile.createNewFile();
            outFileStream = new FileOutputStream(storeFile);
            buffstream = new BufferedOutputStream(outFileStream);
            buffstream.write(mRequestResolution.getBytes());
            buffstream.flush();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (outFileStream != null) {
                try {
                    outFileStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "failed to close dumpsys output stream");
                }
            }
            if (buffstream != null) {
                try {
                    buffstream.close();
                } catch (IOException e) {
                    Log.e(TAG, "failed to close dumpsys output stream");
                }
            }
        }
        return true;
    }

    private void updateResolution() {
        mCurrentResolution = 0;
        mResolutionEntryValues.clear();
        mResolutionEntries.clear();
        mResolutionDpis.clear();
        mResolutionDpis.add("240");
        mResolutionDpis.add("320");
        mResolutionEntries.add("1600x1200");
        mResolutionEntries.add("2048x1536");
        mResolutionEntryValues.add("0");
        mResolutionEntryValues.add("1");
        Log.d(TAG, "current resolution:" + mCurrentResolution);
        Log.d(TAG, "mRequestResolution resolution:" + mRequestResolution);
        Log.d(TAG, "entries:" + mResolutionEntries);
        Log.d(TAG, "values:" + mResolutionEntryValues);
        Log.d(TAG, "dpis:" + mResolutionDpis);

        FileInputStream input = null;
        File filename = new File(DUMPSYS_DATA_PATH+STORE_RESOLUTION_PATH+".bin");
        if (filename.exists()) {
            try {
                input = new FileInputStream(filename);
                byte[] buffer = new byte[(int) filename.length()];
                input.read(buffer);
                mCurrentResolution = Integer.parseInt(new String(buffer));
            } catch (IOException e) {
                Log.w(TAG, "Can't read service dump: ", e);
            } finally {
                if (input != null) try { input.close(); } catch (IOException e) {}
            }
        } else {
            mCurrentResolution = 1;
        }

        mResolutionPreference.setEntryValues(
            mResolutionEntryValues.toArray(new CharSequence[mResolutionEntryValues.size()]));
        mResolutionPreference.setValueIndex(mCurrentResolution);
        mResolutionPreference.setSummary(String.format(
            getResources().getString(R.string.summary_resolution),
            mResolutionPreference.getEntry()));
    }

    private void showRebootDialog() {
        removeDialog(DLG_CONFIRM_REBOOT);
        showDialog(DLG_CONFIRM_REBOOT);
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        switch (dialogId) {
        case DLG_CONFIRM_REBOOT:
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dlg_confirm_reboot_title)
                    .setPositiveButton(R.string.dlg_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (!applyResolution()) {
                                    Toast.makeText(getActivity(),
                                        R.string.set_resolution_failed_message, 0)
                                        .show();
                                } else {
                                    updateResolution();
                                    SystemProperties.set("persist.sys.lcd_density",
                                        (String)(mResolutionDpis.get(
                                                 mResolutionEntryValues.indexOf(
                                                 mRequestResolution))));
                                    PowerManager pm = (PowerManager)
                                        mResolutionPreference.getContext()
                                        .getSystemService(Context.POWER_SERVICE);
                                    pm.reboot(KEY_RESOLUTION);
                                }
                            }
                        })
                    .setNegativeButton(R.string.cancel, null)
                    .setMessage(R.string.dlg_confirm_reboot_text)
                    .create();
        case DLG_GLOBAL_CHANGE_WARNING:
            return Utils.buildGlobalChangeWarningDialog(getActivity(),
                    R.string.global_font_change_title,
                    new Runnable() {
                        public void run() {
                        }
                    });
        }
        return super.onCreateDialog(dialogId);
    }

    private final RotationPolicy.RotationPolicyListener mRotationPolicyListener =
            new RotationPolicy.RotationPolicyListener() {
        @Override
        public void onChange() {
            updateAccelerometerRotationCheckbox();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver resolver = getActivity().getContentResolver();

        addPreferencesFromResource(R.xml.display_settings);

        mAccelerometer = (CheckBoxPreference) findPreference(KEY_ACCELEROMETER);
        mAccelerometer.setPersistent(false);
        if (RotationPolicy.isRotationLockToggleSupported(getActivity())) {
            // If rotation lock is supported, then we do not provide this option in
            // Display settings.  However, is still available in Accessibility settings.
            getPreferenceScreen().removePreference(mAccelerometer);
        }

        mStatusbarPreference = findPreference(KEY_STATUSBAR);
        mOpenEpubPreference = findPreference(KEY_OPEN_EPUB);
        mVolumeButtonsPreference = findPreference(KEY_VOLUME_BUTTONS);
        mA2FlushPreference = findPreference(KEY_A2_FLUSH);
        mPicrecoverPreference = findPreference(KEY_PIC_RECOVER);

        String customer = SystemProperties.get("ro.boeye.customer");
        boolean etaCustomer = customer.toLowerCase().contains("eta");
        if (etaCustomer) {
            getPreferenceScreen().removePreference(mVolumeButtonsPreference);
        }
        getPreferenceScreen().removePreference(mA2FlushPreference);
        getPreferenceScreen().removePreference(mAccelerometer);

        mScreenSaverPreference = findPreference(KEY_SCREEN_SAVER);
        if (mScreenSaverPreference != null
                && getResources().getBoolean(
                        com.android.internal.R.bool.config_dreamsSupported) == false) {
            getPreferenceScreen().removePreference(mScreenSaverPreference);
        }
        getPreferenceScreen().removePreference(mScreenSaverPreference);
        
        mScreenTimeoutPreference = (ListPreference) findPreference(KEY_SCREEN_TIMEOUT);
        final long currentTimeout = SystemProperties.getLong("persist.boeye.shortstandby", 60000);
        mScreenTimeoutPreference.setValue(String.valueOf(currentTimeout));
        mScreenTimeoutPreference.setOnPreferenceChangeListener(this);
        disableUnusableTimeouts(mScreenTimeoutPreference);
        updateTimeoutPreferenceDescription(currentTimeout);

        mShutdownDelayPreference = (ListPreference) findPreference(KEY_SHUTDOWN_DELAY);
        int shutdownDelay = SystemProperties.getInt("persist.boeye.shutdowntime", 999);
        if (shutdownDelay < 0 || shutdownDelay > 120) {
            shutdownDelay = 999;
        }
        mShutdownDelayPreference.setValue(String.valueOf(shutdownDelay));
        mShutdownDelayPreference.setOnPreferenceChangeListener(this);
        updateShutdownDelayDescription(shutdownDelay);

        mFullscreenFlushPreference = (ListPreference) findPreference(KEY_FULLSCREEN_FLUSH);
        int times = Settings.System.getInt(resolver, KEY_FULLSCREEN_FLUSH, 5);
        if (times < 1 || times > 10) {
            times = 5;
        }
        mFullscreenFlushPreference.setValue(String.valueOf(times));
        mFullscreenFlushPreference.setOnPreferenceChangeListener(this);
        updateFullscreenFlushDescription(times);

        mFontSizePref = (WarnedListPreference) findPreference(KEY_FONT_SIZE);
        mFontSizePref.setOnPreferenceChangeListener(this);
        mFontSizePref.setOnPreferenceClickListener(this);

        getPreferenceScreen().removePreference(mFontSizePref);
        if (SystemProperties.getInt("persist.boeye.statusbar", 1) == 1) {
            mStatusbarPreference.setSummary(R.string.statusbar_summary_on);
        } else {
            mStatusbarPreference.setSummary(R.string.statusbar_summary_off);
        }
        if (Settings.System.getInt(getContentResolver(), "open_epub", 0) == 0) {
            mOpenEpubPreference.setSummary(R.string.open_epub_withPDF);
        } else {
            mOpenEpubPreference.setSummary(R.string.open_epub_withFB2);
        }
        if (SystemProperties.getInt("persist.sys.keyChange", 0) == 1) {
            mVolumeButtonsPreference.setSummary(R.string.volume_buttons_on);
        } else {
            mVolumeButtonsPreference.setSummary(R.string.volume_buttons_off);
        }
        if (SystemProperties.getInt("persist.sys.A2Flush", 0) == 1) {
            mA2FlushPreference.setSummary(R.string.volume_buttons_on);
        } else {
            mA2FlushPreference.setSummary(R.string.volume_buttons_off);
        }

        mDisplayManager = (DisplayManager)getActivity().getSystemService(
                Context.DISPLAY_SERVICE);
        boolean isChange = "true".equals(SystemProperties.get(
            "sys.resolution.changed", "false"));
        mResolutionPreference = (ListPreference) findPreference(KEY_RESOLUTION);
        if (isChange) {
            mResolutionPreference.setOnPreferenceChangeListener(this);
            updateResolution();
        } else {
            getPreferenceScreen().removePreference(mResolutionPreference);
        }
    }

    private void updateShutdownDelayDescription(int currentTimeout) {
        ListPreference preference = mShutdownDelayPreference;
        String summary;
        if (currentTimeout < 0) {
            summary = "";
        } else {
            final CharSequence[] entries = preference.getEntries();
            final CharSequence[] values = preference.getEntryValues();
            if (entries == null || entries.length == 0) {
                summary = "";
            } else {
                int best = 0;
                for (int i = 0; i < values.length; i++) {
                    int timeout = Integer.parseInt(values[i].toString());
                    if (currentTimeout >= timeout) {
                        best = i;
                    }
                }
                summary = preference.getContext()
                    .getString(R.string.screen_timeout_summary, entries[best]);
                if (best == values.length - 1) {
                    summary = new StringBuffer(entries[best]).toString();
                }
            }
        }
        preference.setSummary(summary);
    }

    private void updateFullscreenFlushDescription(int times) {
        ListPreference preference = mFullscreenFlushPreference;
        String summary;
        if (times < 0) {
            summary = "";
        } else {
            final CharSequence[] entries = preference.getEntries();
            final CharSequence[] values = preference.getEntryValues();
            if (entries == null || entries.length == 0) {
                summary = "";
            } else {
                int best = 0;
                for (int i = 0; i < values.length; i++) {
                    int temp = Integer.parseInt(values[i].toString());
                    if (times >= temp) {
                        best = i;
                    }
                }
                summary = new StringBuffer(entries[best]).toString();
            }
        }
        preference.setSummary(summary);
    }

    private void updateTimeoutPreferenceDescription(long currentTimeout) {
        ListPreference preference = mScreenTimeoutPreference;
        String summary;
        if (currentTimeout < 0) {
            // Unsupported value
            summary = "";
        } else {
            final CharSequence[] entries = preference.getEntries();
            final CharSequence[] values = preference.getEntryValues();
            if (entries == null || entries.length == 0) {
                summary = "";
            } else {
                int best = 0;
                for (int i = 0; i < values.length; i++) {
                    long timeout = Long.parseLong(values[i].toString());
                    if (currentTimeout >= timeout) {
                        best = i;
                    }
                }
                summary = preference.getContext().getString(R.string.screen_timeout_summary,
                        entries[best]);
            }
        }
        preference.setSummary(summary);
    }

    private void disableUnusableTimeouts(ListPreference screenTimeoutPreference) {
        final DevicePolicyManager dpm =
                (DevicePolicyManager) getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        final long maxTimeout = dpm != null ? dpm.getMaximumTimeToLock(null) : 0;
        if (maxTimeout == 0) {
            return; // policy not enforced
        }
        final CharSequence[] entries = screenTimeoutPreference.getEntries();
        final CharSequence[] values = screenTimeoutPreference.getEntryValues();
        ArrayList<CharSequence> revisedEntries = new ArrayList<CharSequence>();
        ArrayList<CharSequence> revisedValues = new ArrayList<CharSequence>();
        for (int i = 0; i < values.length; i++) {
            long timeout = Long.parseLong(values[i].toString());
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
            final int userPreference = Integer.parseInt(screenTimeoutPreference.getValue());
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

    int floatToIndex(float val) {
        String[] indices = getResources().getStringArray(R.array.entryvalues_font_size);
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
    
    public void readFontSizePreference(ListPreference pref) {
        try {
            mCurConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to retrieve font size");
        }

        // mark the appropriate item in the preferences list
        int index = floatToIndex(mCurConfig.fontScale);
        pref.setValueIndex(index);

        // report the current size in the summary text
        final Resources res = getResources();
        String[] fontSizeNames = res.getStringArray(R.array.entries_font_size);
        pref.setSummary(String.format(res.getString(R.string.summary_font_size),
                fontSizeNames[index]));
    }
    
    @Override
    public void onResume() {
        super.onResume();

        RotationPolicy.registerRotationPolicyListener(getActivity(),
                mRotationPolicyListener);

        if (mWifiDisplayPreference != null) {
            getActivity().registerReceiver(mReceiver, new IntentFilter(
                    DisplayManager.ACTION_WIFI_DISPLAY_STATUS_CHANGED));
            mWifiDisplayStatus = mDisplayManager.getWifiDisplayStatus();
        }

        updateState();
    }

    @Override
    public void onPause() {
        super.onPause();

        RotationPolicy.unregisterRotationPolicyListener(getActivity(),
                mRotationPolicyListener);

        if (mWifiDisplayPreference != null) {
            getActivity().unregisterReceiver(mReceiver);
        }
    }

    private void updateState() {
        updateAccelerometerRotationCheckbox();
        updateScreenSaverSummary();
        updateWifiDisplaySummary();
    }

    private void updateScreenSaverSummary() {
        if (mScreenSaverPreference != null) {
            mScreenSaverPreference.setSummary(
                    DreamSettings.getSummaryTextWithDreamName(getActivity()));
        }
    }

    private void updateWifiDisplaySummary() {
        if (mWifiDisplayPreference != null) {
            switch (mWifiDisplayStatus.getFeatureState()) {
                case WifiDisplayStatus.FEATURE_STATE_OFF:
                    mWifiDisplayPreference.setSummary(R.string.wifi_display_summary_off);
                    break;
                case WifiDisplayStatus.FEATURE_STATE_ON:
                    mWifiDisplayPreference.setSummary(R.string.wifi_display_summary_on);
                    break;
                case WifiDisplayStatus.FEATURE_STATE_DISABLED:
                default:
                    mWifiDisplayPreference.setSummary(R.string.wifi_display_summary_disabled);
                    break;
            }
        }
    }

    private void updateAccelerometerRotationCheckbox() {
        if (getActivity() == null) return;

        mAccelerometer.setChecked(!RotationPolicy.isRotationLocked(getActivity()));
    }

    public void writeFontSizePreference(Object objValue) {
        try {
            mCurConfig.fontScale = Float.parseFloat(objValue.toString());
            ActivityManagerNative.getDefault().updatePersistentConfiguration(mCurConfig);
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to save font size");
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mAccelerometer) {
            RotationPolicy.setRotationLockForAccessibility(
                    getActivity(), !mAccelerometer.isChecked());
        } else if (preference == mNotificationPulse) {
            boolean value = mNotificationPulse.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.NOTIFICATION_LIGHT_PULSE,
                    value ? 1 : 0);
            return true;
        } else if (preference == mStatusbarPreference) {
            if (mStatusbarPreference.getSummary().toString().equals(
                        getResources().getString(R.string.statusbar_summary_on))) {
                mStatusbarPreference.setSummary(R.string.statusbar_summary_off);
                android.os.SystemProperties.set("persist.boeye.statusbar", "0");
                Intent intent = new Intent("boyue.hide.status.bar");
                getActivity().sendBroadcast(intent);
            } else {
                mStatusbarPreference.setSummary(R.string.statusbar_summary_on);
                android.os.SystemProperties.set("persist.boeye.statusbar", "1");
                Intent intent = new Intent("boyue.show.status.bar");
                getActivity().sendBroadcast(intent);
            }
        } else if (preference == mOpenEpubPreference) {
            if (Settings.System.getInt(getContentResolver(), "open_epub", 0) == 0) {
                mOpenEpubPreference.setSummary(R.string.open_epub_withFB2);
                Settings.System.putInt(getContentResolver(), "open_epub", 1);
            } else {
                mOpenEpubPreference.setSummary(R.string.open_epub_withPDF);
                Settings.System.putInt(getContentResolver(), "open_epub", 0);
            }
        } else if (preference == mVolumeButtonsPreference) {
            if (SystemProperties.getInt("persist.sys.keyChange", 0) == 1) {
                mVolumeButtonsPreference.setSummary(R.string.volume_buttons_off);
                SystemProperties.set("persist.sys.keyChange", "0");
            } else {
                mVolumeButtonsPreference.setSummary(R.string.volume_buttons_on);
                SystemProperties.set("persist.sys.keyChange", "1");
            }
        } else if (preference == mA2FlushPreference) {
            if (SystemProperties.getInt("persist.sys.A2Flush", 0) == 1) {
                SystemProperties.set("persist.sys.A2Flush", "0");
                mA2FlushPreference.setSummary(R.string.volume_buttons_off);
                getActivity().getWindow().getDecorView()
                    .invalidate(-2023, -2023, -2023, -2023);
                getActivity().getWindow().getDecorView()
                    .invalidate(-2021, -2021, -2021, -2021);
            } else {
                getActivity().getWindow().getDecorView()
                    .invalidate(-2021, -2021, -2021, -2021);
                SystemProperties.set("persist.sys.A2Flush", "1");
                mA2FlushPreference.setSummary(R.string.volume_buttons_on);
                getActivity().getWindow().getDecorView()
                    .invalidate(-2022, -2022, -2022, -2022);
            }
        } else if (preference == mPicrecoverPreference) {
            try {
                Runtime.getRuntime().exec("cp /system/media/standby.png /data/misc/standby.png");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (KEY_SCREEN_TIMEOUT.equals(key)) {
            int value = Integer.parseInt((String) objValue);
            try {
                Settings.System.putInt(getContentResolver(), SCREEN_OFF_TIMEOUT, value);
                SystemProperties.set("persist.boeye.shortstandby", Integer.toString(value));
                updateTimeoutPreferenceDescription(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist screen timeout setting", e);
            }
        }
        if (KEY_SHUTDOWN_DELAY.equals(key)) {
            int value = Integer.parseInt((String) objValue);
            try {
                SystemProperties.set("persist.boeye.shutdowntime", Integer.toString(value));
                updateShutdownDelayDescription(value);
                Intent intent = new Intent();
                intent.setAction("com.android.settings.myshutdown.myShutdownService");
                getActivity().startService(intent);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist Shutdown Delay setting", e);
            }
        }
        if (KEY_FULLSCREEN_FLUSH.equals(key)) {
            int value = Integer.parseInt((String) objValue);
            try {
                Settings.System.putInt(getContentResolver(), KEY_FULLSCREEN_FLUSH, value);
                updateFullscreenFlushDescription(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist screen timeout setting", e);
            }
        }
        if (KEY_FONT_SIZE.equals(key)) {
            writeFontSizePreference(objValue);
        }
        if (KEY_RESOLUTION.equals(key)) {
            mRequestResolution = objValue.toString();
            if (mCurrentResolution != mResolutionEntryValues.indexOf(mRequestResolution)) {
                showRebootDialog();
            }
            return false;
        }

        return true;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DisplayManager.ACTION_WIFI_DISPLAY_STATUS_CHANGED)) {
                mWifiDisplayStatus = (WifiDisplayStatus)intent.getParcelableExtra(
                        DisplayManager.EXTRA_WIFI_DISPLAY_STATUS);
                updateWifiDisplaySummary();
            }
        }
    };

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mFontSizePref) {
            if (Utils.hasMultipleUsers(getActivity())) {
                showDialog(DLG_GLOBAL_CHANGE_WARNING);
                return true;
            }
        }
        return false;
    }
}
