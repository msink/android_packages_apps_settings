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
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

public class Settings extends PreferenceActivity {

    private static final String KEY_PARENT = "parent";
    private static final String KEY_SYNC_SETTINGS = "sync_settings";
    private Context mContext;
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
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        registerReceiver(mReceiver, intentFilter);
    }
    
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mUpdateMedia) {
            mHandler.post(mLoadingRunnable);
            Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED,
                                       android.net.Uri.parse("file://storage"));
            intent.putExtra("read-only", false);
            sendBroadcast(intent);
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
