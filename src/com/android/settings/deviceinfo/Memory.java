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

package com.android.settings.deviceinfo;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Environment;
import android.os.storage.IMountService;
import android.os.ServiceManager;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageEventListener;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import com.android.settings.ChildTitlePreference;
import com.android.settings.R;

import java.io.File;
import java.util.List;

public class Memory extends PreferenceActivity implements OnCancelListener {
    private static final String TAG = "Memory";
    private static final boolean localLOGV = false;

    private static final String MEMORY_SD_SIZE = "memory_sd_size";
    private static final String MEMORY_SD_AVAIL = "memory_sd_avail";
    private static final String MEMORY_SD_MOUNT_TOGGLE = "memory_sd_mount_toggle";
    private static final String MEMORY_SD_FORMAT = "memory_sd_format";
    private static final String MEMORY_SD_GROUP = "memory_sd";

    private static final String MEMORY_NAND_SIZE = "memory_nand_size";
    private static final String MEMORY_NAND_AVAIL = "memory_nand_avail";
    private static final String MEMORY_NAND_FORMAT = "memory_nand_format";
    private static final String MEMORY_NAND_GROUP = "memory_nand";

    private static final int DLG_CONFIRM_UNMOUNT = 1;
    private static final int DLG_ERROR_UNMOUNT = 2;

    private Resources mRes;

    private Preference mSdSize;
    private Preference mSdAvail;
    private Preference mSdMountToggle;
    private Preference mSdFormat;
    private PreferenceGroup mSdMountPreferenceGroup;

    boolean mSdMountToggleAdded = true;
    
    private Preference mNandSize;
    private Preference mNandAvail;
    private Preference mNandFormat;
    boolean mNandMountToggleAdded = true;

    // Access using getMountService()
    private IMountService mMountService = null;

    private StorageManager mStorageManager = null;

    private ChildTitlePreference preferenceBackSettings;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if (mStorageManager == null) {
            mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
            mStorageManager.registerListener(mStorageListener);
        }

        addPreferencesFromResource(R.xml.device_info_memory);
        
        getListView().setDividerHeight(-1);
        getListView().setDivider(null);
        preferenceBackSettings = (ChildTitlePreference) findPreference("memorysettings_back");

        mRes = getResources();
        mSdSize = findPreference(MEMORY_SD_SIZE);
        mSdAvail = findPreference(MEMORY_SD_AVAIL);
        mSdMountToggle = findPreference(MEMORY_SD_MOUNT_TOGGLE);
        mSdFormat = findPreference(MEMORY_SD_FORMAT);
        mNandSize = findPreference(MEMORY_NAND_SIZE);
        mNandAvail = findPreference(MEMORY_NAND_AVAIL);
        mNandFormat = findPreference(MEMORY_NAND_FORMAT);

        mSdMountPreferenceGroup = (PreferenceGroup)findPreference(MEMORY_SD_GROUP);
        mSdMountPreferenceGroup = (PreferenceGroup)findPreference(MEMORY_NAND_GROUP);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        registerReceiver(mReceiver, intentFilter);

        updateMemoryStatus(Environment.getFlashStorageDirectory().getPath());
        updateMemoryStatus(Environment.getExternalStorageDirectory().getPath());
    }

    StorageEventListener mStorageListener = new StorageEventListener() {

        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
            Log.i(TAG, "Received storage state changed notification that " +
                    path + " changed state from " + oldState +
                    " to " + newState);
            if (path.equals(Environment.getExternalStorageDirectory().getPath()) &&
                    !newState.equals(Environment.MEDIA_MOUNTED)) {
                sdcardUnavailable(newState);
            } else {
                updateMemoryStatus(Environment.getFlashStorageDirectory().getPath());
                updateMemoryStatus(Environment.getExternalStorageDirectory().getPath());
            }
        }
    };
    
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        if (mStorageManager != null && mStorageListener != null) {
            mStorageManager.unregisterListener(mStorageListener);
        }
        super.onDestroy();
    }

    private synchronized IMountService getMountService() {
       if (mMountService == null) {
           IBinder service = ServiceManager.getService("mount");
           if (service != null) {
               mMountService = IMountService.Stub.asInterface(service);
           } else {
               Log.e(TAG, "Can't get mount service");
           }
       }
       return mMountService;
    }
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSdMountToggle) {
            String status = SystemProperties.get("EXTERNAL_STORAGE_STATE",
                                                 Environment.MEDIA_UNMOUNTED);
            if (status.equals(Environment.MEDIA_MOUNTED)) {
                unmount();
            } else {
                mount();
            }
            return true;
        } else if (preference == mSdFormat) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClass(this, com.android.settings.MediaFormat.class);
            intent.putExtra("path", Environment.getExternalStorageDirectory().getPath());
            startActivity(intent);
            return true;
        } else if (preference == mNandFormat) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClass(this, com.android.settings.MediaFormat.class);
            intent.putExtra("path", Environment.getFlashStorageDirectory().getPath());
            startActivity(intent);
        } else if (preference == preferenceBackSettings) {
            finish();
        }
        
        return false;
    }
     
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        }
    };

    @Override
    public Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
        case DLG_CONFIRM_UNMOUNT:
            return new AlertDialog.Builder(this)
                    .setTitle(R.string.dlg_confirm_unmount_title)
                    .setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            doUnmount(true);
                        }})
                    .setNegativeButton(R.string.cancel, null)
                    .setMessage(R.string.dlg_confirm_unmount_text)
                    .setOnCancelListener(this)
                    .create();
        case DLG_ERROR_UNMOUNT:
            return new AlertDialog.Builder(this                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            )
            .setTitle(R.string.dlg_error_unmount_title)
            .setNeutralButton(R.string.dlg_ok, null)
            .setMessage(R.string.dlg_error_unmount_text)
            .setOnCancelListener(this)
            .create();
        }
        return null;
    }

    private void doUnmount(boolean force) {
        // Present a toast here
        Toast.makeText(this, R.string.unmount_inform_text, Toast.LENGTH_SHORT).show();
        IMountService mountService = getMountService();
        String extStoragePath = Environment.getExternalStorageDirectory().toString();
        try {
            mSdMountToggle.setEnabled(false);
            mSdMountToggle.setTitle(mRes.getString(R.string.sd_ejecting_title));
            mSdMountToggle.setSummary(mRes.getString(R.string.sd_ejecting_summary));
            mountService.unmountVolume(extStoragePath, force);
        } catch (RemoteException e) {
            // Informative dialog to user that
            // unmount failed.
            showDialogInner(DLG_ERROR_UNMOUNT);
        }
    }

    private void showDialogInner(int id) {
        removeDialog(id);
        showDialog(id);
    }

    private boolean hasAppsAccessingStorage() throws RemoteException {
        String extStoragePath = Environment.getExternalStorageDirectory().toString();
        IMountService mountService = getMountService();
        int stUsers[] = mountService.getStorageUsers(extStoragePath);
        if (stUsers != null && stUsers.length > 0) {
            return true;
        }
        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<ApplicationInfo> list = am.getRunningExternalApplications();
        if (list != null && list.size() > 0) {
            return true;
        }
        return false;
    }

    private void unmount() {
        // Check if external media is in use.
        try {
           if (hasAppsAccessingStorage()) {
               if (localLOGV) Log.i(TAG, "Do have storage users accessing media");
               // Present dialog to user
               showDialogInner(DLG_CONFIRM_UNMOUNT);
           } else {
               doUnmount(true);
           }
        } catch (RemoteException e) {
            // Very unlikely. But present an error dialog anyway
            Log.e(TAG, "Is MountService running?");
            showDialogInner(DLG_ERROR_UNMOUNT);
        }
    }

    private void mount() {
        IMountService mountService = getMountService();
        try {
            if (mountService != null) {
                mountService.mountVolume(Environment.getExternalStorageDirectory().toString());
            } else {
                Log.e(TAG, "Mount service is null, can't mount");
            }
        } catch (RemoteException ex) {
        }
    }

    private void sdcardUnavailable(String status) {
        Log.d(TAG, "set sdcardUnavailable");
        mSdSize.setSummary(mRes.getString(R.string.sd_unavailable));
        mSdAvail.setSummary(mRes.getString(R.string.sd_unavailable));
        if (!Environment.isExternalStorageRemovable() &&
                status.equals(Environment.MEDIA_UNMOUNTED) && !mSdMountToggleAdded) {
            mSdMountPreferenceGroup.addPreference(mSdMountToggle);
            mSdMountToggleAdded = true;
        }
        if (status.equals(Environment.MEDIA_UNMOUNTED) ||
                status.equals(Environment.MEDIA_NOFS) ||
                status.equals(Environment.MEDIA_UNMOUNTABLE)) {
            mSdMountToggle.setEnabled(true);
            mSdMountToggle.setTitle(mRes.getString(R.string.sd_mount));
            mSdMountToggle.setSummary(mRes.getString(R.string.sd_mount_summary));
        } else {
            mSdMountToggle.setEnabled(false);
            mSdMountToggle.setTitle(mRes.getString(R.string.sd_mount));
            mSdMountToggle.setSummary(mRes.getString(R.string.sd_insert_summary));
            mSdFormat.setEnabled(false);
        }
    }

    private void updateMemoryStatus(String path) {
        String status = SystemProperties.get("EXTERNAL_STORAGE_STATE",
                                             Environment.MEDIA_UNMOUNTED);
        if (path.equals(Environment.getFlashStorageDirectory().getPath())) {
            status = Environment.getFlashStorageState();
        }

        String readOnly = "";
        if (status.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            status = Environment.MEDIA_MOUNTED;
            readOnly = mRes.getString(R.string.read_only);
        }
 
        if (status.equals(Environment.MEDIA_MOUNTED)) {

            if (path.equals(android.os.Environment.getExternalStorageDirectory().getPath())) {
                if (!Environment.isExternalStorageRemovable()) {
                    if (mSdMountToggleAdded) {
                        mSdMountPreferenceGroup.removePreference(mSdMountToggle);
                        mSdMountToggleAdded = false;
                    }
                }
            } else {
                if (!Environment.isFlashStorageRemovable()) {
                    if (mSdMountToggleAdded) {
                        mSdMountPreferenceGroup.removePreference(mSdMountToggle);
                        mSdMountToggleAdded = false;
                    }
                }
            }

            try {
                StatFs stat = new StatFs(path);
                long blockSize = stat.getBlockSize();
                long totalBlocks = stat.getBlockCount();
                long availableBlocks = stat.getAvailableBlocks();

                if (path.equals(Environment.getExternalStorageDirectory().getPath())) {
                    mSdSize.setSummary(formatSize(totalBlocks * blockSize));
                    mSdAvail.setSummary(formatSize(availableBlocks * blockSize) + readOnly);
                    mSdMountToggle.setEnabled(true);
                    mSdMountToggle.setTitle(mRes.getString(R.string.sd_eject));
                    mSdMountToggle.setSummary(mRes.getString(R.string.sd_eject_summary));
                    mSdFormat.setEnabled(true);
                } else {
                    mNandSize.setSummary(formatSize(totalBlocks * blockSize));
                    mNandAvail.setSummary(formatSize(availableBlocks * blockSize) + readOnly);
                    mNandFormat.setEnabled(true);
                }

            } catch (IllegalArgumentException e) {
                // this can occur if the SD card is removed, but we haven't received the 
                // ACTION_MEDIA_REMOVED Intent yet.
                status = Environment.MEDIA_REMOVED;
            }
            
        } else {
            if (path.equals(Environment.getExternalStorageDirectory().getPath())) {
                mSdSize.setSummary(mRes.getString(R.string.sd_unavailable));
                mSdAvail.setSummary(mRes.getString(R.string.sd_unavailable));

                if (!Environment.isExternalStorageRemovable()) {
                    if (status.equals(Environment.MEDIA_UNMOUNTED)) {
                        if (!mSdMountToggleAdded) {
                            mSdMountPreferenceGroup.addPreference(mSdMountToggle);
                            mSdMountToggleAdded = true;
                        }
                    }
                }

                if (status.equals(Environment.MEDIA_UNMOUNTED) ||
                    status.equals(Environment.MEDIA_NOFS) ||
                    status.equals(Environment.MEDIA_UNMOUNTABLE) ) {
                    mSdMountToggle.setEnabled(true);
                    mSdMountToggle.setTitle(mRes.getString(R.string.sd_mount));
                    mSdMountToggle.setSummary(mRes.getString(R.string.sd_mount_summary));
                    mSdFormat.setEnabled(true);
                } else {
                    mSdMountToggle.setEnabled(false);
                    mSdMountToggle.setTitle(mRes.getString(R.string.sd_mount));
                    mSdMountToggle.setSummary(mRes.getString(R.string.sd_insert_summary));
                    mSdFormat.setEnabled(false);
                }

            } else {

                mNandSize.setSummary(mRes.getString(R.string.nand_unavailable));
                mNandAvail.setSummary(mRes.getString(R.string.nand_unavailable));

                if (status.equals(Environment.MEDIA_UNMOUNTED) ||
                        status.equals(Environment.MEDIA_NOFS) ||
                        status.equals(Environment.MEDIA_UNMOUNTABLE) ) {
                    mNandFormat.setEnabled(true);
                } else {
                    mNandFormat.setEnabled(false);
                }

            }
        }

        File dir = Environment.getDataDirectory();
        StatFs stat = new StatFs(dir.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        findPreference("memory_internal_avail").setSummary(formatSize(availableBlocks * blockSize));

    }
    
    private String formatSize(long size) {
        return Formatter.formatFileSize(this, size);
    }

    public void onCancel(DialogInterface dialog) {
        finish();
    }
}
