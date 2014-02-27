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

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

public class Settings extends PreferenceActivity {

    private static final String KEY_PARENT = "parent";
    private static final String KEY_SYNC_SETTINGS = "sync_settings";

    private Context mContext;
    private Preference mDrmSettings;
    private Dialog mMediaScanningDialog;
    private BroadcastReceiver mReceiver;
    private Preference mUpdateMedia;

    private Handler mHandler = new Handler();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.settings);

        PreferenceGroup parent = (PreferenceGroup) findPreference(KEY_PARENT);
        Utils.updatePreferenceToSpecificActivityOrRemove(this, parent, KEY_SYNC_SETTINGS, 0);

        mDrmSettings = findPreference("drm_settings");

        Preference voiceSettings = parent.findPreference("voice_settings");
        if (!SystemProperties.getBoolean("ro.service.tts.enabled", false)) {
            parent.removePreference(voiceSettings);
        }

        if (!SystemProperties.getBoolean("ro.caration.sound.enabled", false)) {
            Preference soundSettings = parent.findPreference("sound_settings");
            parent.removePreference(soundSettings);
        }

        if (!SystemProperties.getBoolean("ro.caration.wifi.enabled", false)) {
            Preference wifiSettings = parent.findPreference("wifi_settings");
            parent.removePreference(wifiSettings);
        }

        if (!SystemProperties.getBoolean("ro.caration.drmdownload.ebabled", false)) {
            Preference drmSettingsTemp = parent.findPreference("drm_settings");
            parent.removePreference(drmSettingsTemp);
        }

        mUpdateMedia = findPreference("updatemedialib");
        mContext = this;

        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                    mHandler.post(mRun_hide);
                }
            }
        };
      if (SystemProperties.get("ro.caration.product.model").equals("C601TF")) {
        IntentFilter intentFilter = new IntentFilter("caration.action.updateend");
        registerReceiver(mReceiver, intentFilter);
      } else {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        registerReceiver(mReceiver, intentFilter);
      }
    }
    
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mUpdateMedia) {
            mHandler.post(mLoadingRunnable);
          if (SystemProperties.get("ro.caration.product.model").equals("C601TF")) {
            Intent intent = new Intent("caration.action.updatebook");
            sendBroadcast(intent);
          } else {
            Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED,
                                       android.net.Uri.parse("file://storage"));
            intent.putExtra("read-only", false);
            sendBroadcast(intent);
          }
        }
        if (preference == mDrmSettings) {
            Intent drmIntent = new Intent();
            drmIntent.setClassName("com.caration.einkdrm",
                                   "com.caration.einkdrm.DRMSettings");
            drmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(drmIntent);
        }
        return false;
    }

    Runnable mRun_hide = new Runnable() {
        public void run() {
            if (mMediaScanningDialog != null) {
                mMediaScanningDialog.dismiss();
                mMediaScanningDialog = null;
            }
        }
    };

    protected Runnable mLoadingRunnable = new Runnable() {
        public void run() {
            if (mMediaScanningDialog != null) {
                mMediaScanningDialog.cancel();
                mMediaScanningDialog = null;
            }
            mMediaScanningDialog = ProgressDialog.show(mContext,
                getResources().getString(R.string.updatemedialibtitle),
                getResources().getString(R.string.updatemedialibmsg), true, true);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onResume();
        unregisterReceiver(mReceiver);
    }
}
