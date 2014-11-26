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

import com.android.internal.os.storage.ExternalStorageFormatter;
import com.android.internal.widget.LockPatternUtils;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import java.util.Date;

/**
 * Confirm and execute a reset of the device to a clean "just out of the box"
 * state.  Multiple confirmations are required: first, a general "are you sure
 * you want to do this?" prompt, followed by a keyguard pattern trace if the user
 * has defined one, followed by a final strongly-worded "THIS WILL ERASE EVERYTHING
 * ON THE PHONE" prompt.  If at any time the phone is allowed to go to sleep, is
 * locked, et cetera, then the confirmation sequence is abandoned.
 *
 * This is the confirmation screen.
 */
public class MasterClearConfirm extends Fragment {
    private static final String TAG = "MasterClear";

    private View mContentView;
    private boolean mEraseSdCard;
    private Button mFinalButton;
    private Dialog mProgressDialog;
    private PowerManager.WakeLock startupWakeLock;
    private PowerManager pm = null;

    /**
     * The user has gone through the multiple confirmation, so now we go ahead
     * and invoke the Checkin Service to reset the device to its factory-default
     * state (rebooting in the process).
     */
    private Button.OnClickListener mFinalClickListener = new Button.OnClickListener() {

        public void onClick(View v) {
            if (Utils.isMonkeyRunning()) {
                return;
            }

            SystemProperties.set("ctl.start", "remove_test");
            SystemClock.sleep(500);
            SystemProperties.set("ctl.stop", "remove_test");
            if (mContentView != null) {
                mContentView.requestEpdMode(View.EINK_MODE.EPD_FULL);
                SystemClock.sleep(1000);
            }
            mProgressDialog.show();
            acquireStartupWakeLock();
            v.setVisibility(View.INVISIBLE);
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    long time = new Date().getTime();
                    Log.d(TAG, "remove files start : time is " + time);
                    RemoveFlashAllFiles.removeAllFiles();
                    Log.d(TAG, "delete files count is " + RemoveFlashAllFiles.getDeleteFilesCount() +
                        "  time = " + (new Date().getTime() - time));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    getActivity().sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
                }
            });
            thread.start();
        }
    };

    /**
     * Configure the UI for the final confirmation interaction
     */
    private void establishFinalConfirmationState() {
        mFinalButton = (Button) mContentView.findViewById(R.id.execute_master_clear);
        mFinalButton.setOnClickListener(mFinalClickListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.master_clear_confirm, null);
        establishFinalConfirmationState();
        mProgressDialog = new Dialog(getActivity(), R.style.dialog_no_title);
        View tv = inflater.inflate(R.layout.waiting_dialog_view, null);
        mProgressDialog.setContentView(tv);
        mProgressDialog.setCancelable(false);
        return mContentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        Bundle args = getArguments();
        mEraseSdCard = args != null ? args.getBoolean(MasterClear.ERASE_EXTERNAL_EXTRA) : false;
    }

    private void acquireStartupWakeLock() {
        if (startupWakeLock == null) {
            startupWakeLock = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,
                "Master_Clear_WakeLock");
        }
        startupWakeLock.acquire();
    }
}
