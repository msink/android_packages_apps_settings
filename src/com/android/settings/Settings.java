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

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import java.util.Timer;
import java.util.TimerTask;

public class Settings extends PreferenceActivity {

    private static final String KEY_PARENT = "parent";
    private static final String KEY_SYNC_SETTINGS = "sync_settings";

    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    TitlePreference titlePre;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.settings);

        PreferenceGroup parent = (PreferenceGroup) findPreference(KEY_PARENT);
        Utils.updatePreferenceToSpecificActivityOrRemove(this, parent, KEY_SYNC_SETTINGS, 0);

        getListView().setDividerHeight(-1);
        getListView().setDivider(null);

        Preference voiceSettings = parent.findPreference("voice_settings");
        if (!SystemProperties.getBoolean("ro.service.tts.enabled", false)) {
            parent.removePreference(voiceSettings);
        }

        titlePre = (TitlePreference) parent.findPreference("title");
        Help.startInforService(this);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        Help.stopTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Help.startTimerChangeBattery(titlePre);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Help.stopInforService(this);
        Help.stopTimer();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent mIntent = getPackageManager()
                             .getLaunchIntentForPackage("xrz.com.android");
            if (mIntent != null) {
                startActivity(mIntent);
            }
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, msg);
    }
}
