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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Process;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;

public class NewApplicationSettings extends SettingsPreferenceFragment
        implements DialogInterface.OnClickListener {

    private static final String ENABLE_ADB = "enable_adb";
    private static final String TOGGLE_INSTALL = "toggle_install_applications";

    private Dialog mAdbDialog;
    private DialogInterface mWarnInstallApps;
    private CheckBoxPreference mEnableAdb;
    private CheckBoxPreference mToggleAppInstallation;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.new_application_settings);

        mEnableAdb = findAndInitCheckboxPref(ENABLE_ADB);
        mToggleAppInstallation = (CheckBoxPreference) findPreference(TOGGLE_INSTALL);
        mToggleAppInstallation.setChecked(isNonMarketAppsAllowed());

        int adbEnabled = Settings.Global.getInt(getActivity().getContentResolver(),
                Settings.Global.ADB_ENABLED, 0);
        if (adbEnabled > 0) {
            mEnableAdb.setChecked(true);
        }
        if (!Process.myUserHandle().equals(UserHandle.OWNER)) {
            disableForUser(mEnableAdb);
        }
    }

    private void warnAppInstallation() {
        mWarnInstallApps = new AlertDialog.Builder(getActivity())
            .setTitle(getResources().getString(R.string.error_title))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage(getResources().getString(R.string.install_all_warning))
            .setPositiveButton(android.R.string.yes, this)
            .setNegativeButton(android.R.string.no, null)
            .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dismissDialogs();
    }

    private CheckBoxPreference findAndInitCheckboxPref(String key) {
        CheckBoxPreference pref = (CheckBoxPreference) findPreference(key);
        if (pref == null) {
            throw new IllegalArgumentException("Cannot find preference with key = " + key);
        }
        return pref;
    }

    private void dismissDialogs() {
        if (mAdbDialog != null) {
            mAdbDialog.dismiss();
            mAdbDialog = null;
        }
        if (mWarnInstallApps != null) {
            mWarnInstallApps.dismiss();
        }
    }

    private void disableForUser(Preference pref) {
        if (pref != null) {
            pref.setEnabled(false);
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mAdbDialog) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                Settings.Global.putInt(getActivity().getContentResolver(),
                        Settings.Global.ADB_ENABLED, 1);
            } else {
                // Reset the toggle
                mEnableAdb.setChecked(false);
            }
        } else if (dialog == mWarnInstallApps) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                setNonMarketAppsAllowed(true);
                if (mToggleAppInstallation != null) {
                    mToggleAppInstallation.setChecked(true);
                }
            }
        }
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference == mEnableAdb) {
            if (mEnableAdb.isChecked()) {
                if (mAdbDialog != null) dismissDialogs();
                mAdbDialog = new AlertDialog.Builder(getActivity()).setMessage(
                        getActivity().getResources().getString(R.string.adb_warning_message))
                        .setTitle(R.string.adb_warning_title)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setPositiveButton(android.R.string.yes, this)
                        .setNegativeButton(android.R.string.no, this)
                        .show();
            } else {
                Settings.Global.putInt(getActivity().getContentResolver(),
                        Settings.Global.ADB_ENABLED, 0);
            }
        } else if (preference == mToggleAppInstallation) {
            if (mToggleAppInstallation.isChecked()) {
                mToggleAppInstallation.setChecked(false);
                warnAppInstallation();
            } else {
                setNonMarketAppsAllowed(false);
            }
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private boolean isNonMarketAppsAllowed() {
        return Settings.Global.getInt(getContentResolver(),
            Settings.Global.INSTALL_NON_MARKET_APPS, 0) > 0;
    }

    private void setNonMarketAppsAllowed(boolean enabled) {
        Settings.Global.putInt(getContentResolver(),
            Settings.Global.INSTALL_NON_MARKET_APPS,
            enabled ? 1 : 0);
    }
}
