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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.backup.IBackupManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.method.LinkMovementMethod;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * Gesture lock pattern settings.
 */
public class PrivacySettings extends PreferenceActivity implements
        DialogInterface.OnClickListener {

    // Vendor specific
    private static final String GSETTINGS_PROVIDER = "com.google.settings";
    private static final String BACKUP_CATEGORY = "backup_category";
    private static final String BACKUP_DATA = "backup_data";
    private static final String AUTO_RESTORE = "auto_restore";
    private Dialog mConfirmDialog;

    private static final int DIALOG_ERASE_BACKUP = 2;
    private int     mDialogType;
    private ChildTitlePreference preferenceBackSettings;
    private TitlePreference titlePre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.privacy_settings);
        final PreferenceScreen screen = getPreferenceScreen();

        getListView().setDividerHeight(-1);
        getListView().setDivider(null);

        preferenceBackSettings = (ChildTitlePreference) findPreference("privacy_back");
        titlePre = (TitlePreference) findPreference("privacy_title");
        updateToggles();
    }

    @Override
    public void onStop() {
        if (mConfirmDialog != null && mConfirmDialog.isShowing()) {
            mConfirmDialog.dismiss();
        }
        mConfirmDialog = null;
        mDialogType = 0;
        super.onStop();
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
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == preferenceBackSettings) {
            finish();
        }

        return false;
    }

    private void showEraseBackupDialog() {
        mDialogType = DIALOG_ERASE_BACKUP;
        CharSequence msg = getResources().getText(R.string.backup_erase_dialog_message);
        mConfirmDialog = new AlertDialog.Builder(this).setMessage(msg)
                .setTitle(R.string.backup_erase_dialog_title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, this)
                .show();
    }

    /*
     * Creates toggles for each available location provider
     */
    private void updateToggles() {
        ContentResolver res = getContentResolver();

        final boolean backupEnabled = Settings.Secure.getInt(res,
                Settings.Secure.BACKUP_ENABLED, 0) == 1;
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            //updateProviders();
            if (mDialogType == DIALOG_ERASE_BACKUP) {
                setBackupEnabled(false);
            }
        } else {
            if (mDialogType == DIALOG_ERASE_BACKUP) {
            }
        }
        mDialogType = 0;
    }

    /**
     * Informs the BackupManager of a change in backup state - if backup is disabled,
     * the data on the server will be erased.
     * @param enable whether to enable backup
     */
    private void setBackupEnabled(boolean enable) {
        IBackupManager bm = IBackupManager.Stub.asInterface(
                ServiceManager.getService(Context.BACKUP_SERVICE));
        if (bm != null) {
            try {
                bm.setBackupEnabled(enable);
            } catch (RemoteException e) {
                return;
            }
        }
    }
}
