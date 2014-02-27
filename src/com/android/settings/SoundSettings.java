/*
 * Copyright (C) 2007 The Android Open Source Project
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

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.IWindowManager;

public class SoundSettings extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "SoundAndDisplaysSettings";

    /** If there is no setting in the provider, use this. */
    private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 30000;
    private static final int FALLBACK_EMERGENCY_TONE_VALUE = 0;

    private static final String KEY_SILENT = "silent";
    private static final String KEY_VIBRATE = "vibrate";
    private static final String KEY_DTMF_TONE = "dtmf_tone";
    private static final String KEY_SOUND_EFFECTS = "sound_effects";
    private static final String KEY_HAPTIC_FEEDBACK = "haptic_feedback";
    private static final String KEY_EMERGENCY_TONE = "emergency_tone";
    private static final String KEY_SOUND_SETTINGS = "sound_settings";
    private static final String KEY_NOTIFICATION_PULSE = "notification_pulse";
    private static final String KEY_LOCK_SOUNDS = "lock_sounds";

    private static final String VALUE_VIBRATE_NEVER = "never";
    private static final String VALUE_VIBRATE_ALWAYS = "always";
    private static final String VALUE_VIBRATE_ONLY_SILENT = "silent";
    private static final String VALUE_VIBRATE_UNLESS_SILENT = "notsilent";

    private CheckBoxPreference mSilent;

    private AudioManager mAudioManager;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
                updateState(false);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver resolver = getContentResolver();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        addPreferencesFromResource(R.xml.sound_settings);

        mSilent = (CheckBoxPreference) findPreference(KEY_SILENT);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateState(true);

        IntentFilter filter = new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mReceiver);
    }

    // updateState in fact updates the UI to reflect the system state
    private void updateState(boolean force) {
        final int ringerMode = mAudioManager.getRingerMode();

        // NB: in the UI we now simply call this "silent mode". A separate
        // setting controls whether we're in RINGER_MODE_SILENT or
        // RINGER_MODE_VIBRATE.
        final boolean silentOrVibrateMode =
                ringerMode != AudioManager.RINGER_MODE_NORMAL;

        if (silentOrVibrateMode != mSilent.isChecked() || force) {
            mSilent.setChecked(silentOrVibrateMode);
        }

        int silentModeStreams = Settings.System.getInt(getContentResolver(),
                Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);
        boolean isAlarmInclSilentMode = (silentModeStreams & (1 << AudioManager.STREAM_ALARM)) != 0;
        mSilent.setSummary(isAlarmInclSilentMode ?
                R.string.silent_mode_incl_alarm_summary :
                R.string.silent_mode_summary);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSilent) {
            if (mSilent.isChecked()) {
                boolean vibeInSilent = (1 == Settings.System.getInt(
                    getContentResolver(),
                    Settings.System.VIBRATE_IN_SILENT,
                    1));
                mAudioManager.setRingerMode(
                    vibeInSilent ? AudioManager.RINGER_MODE_VIBRATE
                                 : AudioManager.RINGER_MODE_SILENT);
                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            } else {
                mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            }
            updateState(false);
        }

        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        return true;
    }
}
