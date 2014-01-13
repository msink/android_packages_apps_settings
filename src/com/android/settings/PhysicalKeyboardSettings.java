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

import android.content.ContentResolver;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings.System;
import android.view.Window;
import android.view.WindowManager;

public class PhysicalKeyboardSettings extends PreferenceActivity {
    
    private final String[] mSettingsUiKey = {
            "auto_caps",
            "auto_replace",
            "auto_punctuate",
    };
    
    // Note: Order of this array should correspond to the order of the above array
    private final String[] mSettingsSystemId = {
            System.TEXT_AUTO_CAPS,
            System.TEXT_AUTO_REPLACE,
            System.TEXT_AUTO_PUNCTUATE,
    };

    // Note: Order of this array should correspond to the order of the above array
    private final int[] mSettingsDefault = {
            1,
            1,
            1,
    };

    private Drawable mTopDrawable;
    private Drawable mMidDrawable;
    private Drawable mBotDrawable;

    @Override
    protected void onCreate(Bundle icicle) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(icicle);

        mTopDrawable = getResources().getDrawable(R.drawable.settings_bg_top);
        mMidDrawable = getResources().getDrawable(R.drawable.settings_bg_mid);
        mBotDrawable = getResources().getDrawable(R.drawable.settings_bg_bottom);

        addPreferencesFromResource(R.xml.keyboard_settings);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        ContentResolver resolver = getContentResolver();
        for (int i = 0; i < mSettingsUiKey.length; i++) {
            MyCheckBoxPreference pref = (MyCheckBoxPreference) findPreference(mSettingsUiKey[i]);
            pref.setChecked(System.getInt(resolver, mSettingsSystemId[i],
                                          mSettingsDefault[i]) > 0);
            //pref.setIcon(mTopDrawable);
        }
    }


    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        // Physical keyboard stuff
        for (int i = 0; i < mSettingsUiKey.length; i++) {
            if (mSettingsUiKey[i].equals(preference.getKey())) {
                System.putInt(getContentResolver(), mSettingsSystemId[i], 
                        ((MyCheckBoxPreference)preference).isChecked()? 1 : 0);
                return true;
            }
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

}
