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

import android.content.Context;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.os.RemoteException;
import android.os.IPowerManager;
import android.os.ServiceManager;
import android.os.Handler;
import android.preference.SeekBarPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import java.util.Map;

public class BrightnessPreference extends SeekBarPreference implements
        SeekBar.OnSeekBarChangeListener, CheckBox.OnCheckedChangeListener {

    private SeekBar mSeekBar;
    private CheckBox mCheckBox;
    
    private int mOldBrightness;
    private int mOldAutomatic;

    private boolean mAutomaticAvailable;
    
    // Backlight range is from 0 - 255. Need to make sure that user
    // doesn't set the backlight to 0 and get stuck
    private static final int MINIMUM_BACKLIGHT = android.os.Power.BRIGHTNESS_DIM + 10;
    private static final int MAXIMUM_BACKLIGHT = android.os.Power.BRIGHTNESS_ON;

    private ContentObserver mContentChangeObserver;
    private Context mContext;

    public BrightnessPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mAutomaticAvailable = context.getResources().getBoolean(
                com.android.internal.R.bool.config_automatic_brightness_available);

        setDialogLayoutResource(R.layout.preference_dialog_brightness);
        setDialogIcon(R.drawable.ic_settings_display);
        mContext = context;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mSeekBar = getSeekBar(view);
        mSeekBar.setMax(MAXIMUM_BACKLIGHT - MINIMUM_BACKLIGHT);
        try {
            mOldBrightness = Settings.System.getInt(getContext().getContentResolver(), 
                Settings.System.SCREEN_BRIGHTNESS);
        } catch (SettingNotFoundException snfe) {
            mOldBrightness = MAXIMUM_BACKLIGHT;
        }
        mSeekBar.setProgress(mOldBrightness - MINIMUM_BACKLIGHT);

        mCheckBox = (CheckBox)view.findViewById(R.id.automatic_mode);
        if (mAutomaticAvailable) {
            mCheckBox.setOnCheckedChangeListener(this);
            try {
                mOldAutomatic = Settings.System.getInt(getContext().getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE);
            } catch (SettingNotFoundException snfe) {
                mOldAutomatic = 0;
            }
            mCheckBox.setChecked(mOldAutomatic != 0);
        } else {
            mCheckBox.setVisibility(View.GONE);
        }
        mSeekBar.setOnSeekBarChangeListener(this);

        mContentChangeObserver = new ContentChangeObserver();
        mContext.getContentResolver().registerContentObserver(
            Settings.System.CONTENT_URI, true, mContentChangeObserver);
    }

    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromTouch) {
        setBrightness(progress + MINIMUM_BACKLIGHT);
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        // NA
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        Settings.System.putInt(getContext().getContentResolver(),
            "screen_brightness", mSeekBar.getProgress() + 30);
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setMode(isChecked ? Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                : Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        if (!isChecked) {
            setBrightness(mSeekBar.getProgress() + MINIMUM_BACKLIGHT);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        mContext.getContentResolver().unregisterContentObserver(mContentChangeObserver);
    }
        
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        if (which == -1) {
            Settings.System.putInt(getContext().getContentResolver(), 
                    Settings.System.SCREEN_BRIGHTNESS,
                    mSeekBar.getProgress() + MINIMUM_BACKLIGHT);
        } else {
            if (mAutomaticAvailable) {
                setMode(mOldAutomatic);
            }
            if (!mAutomaticAvailable || mOldAutomatic == 0) {
                setBrightness(mOldBrightness);
                Settings.System.putInt(getContext().getContentResolver(),
                    "screen_brightness", mOldBrightness);
            }
        }
    }
    
    private void setBrightness(int brightness) {
        try {
            IPowerManager power = IPowerManager.Stub.asInterface(
                    ServiceManager.getService("power"));
            if (power != null) {
                power.setBacklightBrightness(brightness);
            }
        } catch (RemoteException doe) {
            
        }        
    }

    private void setMode(int mode) {
        if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            mSeekBar.setVisibility(View.GONE);
        } else {
            mSeekBar.setVisibility(View.VISIBLE);
        }
        Settings.System.putInt(getContext().getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
    }

    private class ContentChangeObserver extends ContentObserver {
        public ContentChangeObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            updateSetting();
        }
    }

    private void updateSetting() {
        int mOldBrightness;
        try {
            mOldBrightness = Settings.System.getInt(
                mContext.getContentResolver(), "screen_brightness");
        } catch (Settings.SettingNotFoundException snfe) {
            mOldBrightness = 255;
        }
        mSeekBar.setProgress(mOldBrightness - 30);
    }
}

