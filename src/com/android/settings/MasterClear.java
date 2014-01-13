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

import com.android.internal.os.storage.ExternalStorageFormatter;
import com.android.internal.widget.LockPatternUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

/**
 * Confirm and execute a reset of the device to a clean "just out of the box"
 * state.  Multiple confirmations are required: first, a general "are you sure
 * you want to do this?" prompt, followed by a keyguard pattern trace if the user
 * has defined one, followed by a final strongly-worded "THIS WILL ERASE EVERYTHING
 * ON THE PHONE" prompt.  If at any time the phone is allowed to go to sleep, is
 * locked, et cetera, then the confirmation sequence is abandoned.
 */
public class MasterClear extends Activity {

    private static final int KEYGUARD_REQUEST = 55;

    private LayoutInflater mInflater;
    private LockPatternUtils mLockUtils;

    private View mInitialView;
    private Button mInitiateButton;
    private View mExternalStorageContainer;
    private CheckBox mExternalStorage;

    private View mFinalView;
    private Button mFinalButton;

    private ImageView imageBackSettings;
    private RelativeLayout relateBackSetings;
    private ImageView imgBatteryView;
    private ProgressBar mProgressBarWait;
    private int batteryBgResourceID = -1;

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

                if (mExternalStorage.isChecked()) {
                    Intent intent = new Intent(ExternalStorageFormatter.FORMAT_AND_FACTORY_RESET);
                    intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
                    intent.putExtra("path", Environment.getFlashStorageDirectory().getPath());
                    startService(intent);
                } else {
                    sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
                    // Intent handling is asynchronous -- assume it will happen soon.
                    mProgressBarWait.setVisibility(View.VISIBLE);
                    mFinalButton.setEnabled(false);
                }
            }
        };

    /**
     * Keyguard validation is run using the standard {@link ConfirmLockPattern}
     * component as a subactivity
     * @param request the request code to be returned once confirmation finishes
     * @return true if confirmation launched
     */
    private boolean runKeyguardConfirmation(int request) {
        return new ChooseLockSettingsHelper(this)
                .launchConfirmationActivity(request,
                        getText(R.string.master_clear_gesture_prompt),
                        getText(R.string.master_clear_gesture_explanation));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != KEYGUARD_REQUEST) {
            return;
        }
        System.out.println("-shy settings masterClear-----------resultCode--" + resultCode);

        // If the user entered a valid keyguard trace, present the final
        // confirmation prompt; otherwise, go back to the initial state.
        if (resultCode == Activity.RESULT_OK) {
            /*establishFinalConfirmationState()*/;
        } else if (resultCode == Activity.RESULT_CANCELED) {
            System.out.println("-shy settings masterClear-------------------------------");
            finish();
        } else {
            System.out.println("-shy settings onActivityResult-------------------establishInitialState------------");
            /*establishInitialState()*/;
        }
    }

    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int level = intent.getIntExtra("level", 0);
            int scale = intent.getIntExtra("scale", 100);
            int status = intent.getIntExtra("status", 0);
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                switch (status) {
                case BatteryManager.BATTERY_STATUS_UNKNOWN:
                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                default:
                    batteryBgResourceID = getChargingIcon((level * 100) / scale);
                    break;
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    batteryBgResourceID = R.drawable.battery7;
                    break;
                case BatteryManager.BATTERY_STATUS_FULL:
                    batteryBgResourceID = R.drawable.battery1;
                    break;
                }
                imgBatteryView.setBackgroundResource(batteryBgResourceID);
            }
        }
    };

    private int getChargingIcon(int batteryHealth) {
        if (batteryHealth >= 0 && batteryHealth < 20)
            return R.drawable.battery6;
        if (batteryHealth >= 20 && batteryHealth < 40)
            return R.drawable.battery5;
        if (batteryHealth >= 40 && batteryHealth < 60)
            return R.drawable.battery4;
        if (batteryHealth >= 60 && batteryHealth < 80)
            return R.drawable.battery3;
        if (batteryHealth >= 80 && batteryHealth < 95)
            return R.drawable.battery2;
        if (batteryHealth >= 95 && batteryHealth <= 100)
            return R.drawable.battery1;
        else
            return R.drawable.battery6;
    }

    /**
     * If the user clicks to begin the reset sequence, we next require a
     * keyguard confirmation if the user has currently enabled one.  If there
     * is no keyguard available, we simply go to the final confirmation prompt.
     */
    private Button.OnClickListener mInitiateListener = new Button.OnClickListener() {
            public void onClick(View v) {
                if (!runKeyguardConfirmation(KEYGUARD_REQUEST)) {
                    establishFinalConfirmationState();
                }
            }
        };

    /**
     * Configure the UI for the final confirmation interaction
     */
    private void establishFinalConfirmationState() {
        if (mFinalView == null) {
            mFinalView = mInflater.inflate(R.layout.master_clear_final, null);
            mFinalButton =
                    (Button) mFinalView.findViewById(R.id.execute_master_clear);
            mFinalButton.setOnClickListener(mFinalClickListener);
            mFinalButton.requestFocus();
        }

        System.out.println("-shy settings masterClear--------------establishFinalConfirmationState-----------------");
        setContentView(mFinalView);

        imageBackSettings = (ImageView) findViewById(R.id.image_backsettings);
        relateBackSetings = (RelativeLayout) findViewById(R.id.relate_backsettings);
        mProgressBarWait = (ProgressBar) findViewById(R.id.progressBar_Wait);
        imgBatteryView = (ImageView) findViewById(R.id.image_battery);
        imgBatteryView.setBackgroundResource(batteryBgResourceID);
        if (imageBackSettings != null) {
            imageBackSettings.setOnClickListener(new BackSettingsOnClickListener());
        }
        if (relateBackSetings != null) {
            relateBackSetings.setOnClickListener(new BackSettingsOnClickListener());
        }

    }


    /**
     * In its initial state, the activity presents a button for the user to
     * click in order to initiate a confirmation sequence.  This method is
     * called from various other points in the code to reset the activity to
     * this base state.
     *
     * <p>Reinflating views from resources is expensive and prevents us from
     * caching widget pointers, so we use a single-inflate pattern:  we lazy-
     * inflate each view, caching all of the widget pointers we'll need at the
     * time, then simply reuse the inflated views directly whenever we need
     * to change contents.
     */
    private void establishInitialState() {
        if (mInitialView == null) {
            mInitialView = mInflater.inflate(R.layout.master_clear_primary, null);
            mInitiateButton =
                    (Button) mInitialView.findViewById(R.id.initiate_master_clear);
            mInitiateButton.setOnClickListener(mInitiateListener);
            mExternalStorageContainer =
                mInitialView.findViewById(R.id.erase_external_container);
            mExternalStorage =
                    (CheckBox) mInitialView.findViewById(R.id.erase_external);
            mExternalStorageContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mExternalStorage.toggle();
                }
            });
        }

        System.out.println("-shy settings masterClear--------------establishInitialState-----------------");
        setContentView(mInitialView);

        imageBackSettings = (ImageView) findViewById(R.id.image_backsettings);
        relateBackSetings = (RelativeLayout) findViewById(R.id.relate_backsettings);
        imgBatteryView = (ImageView) findViewById(R.id.image_battery);
        if (imageBackSettings != null) {
            imageBackSettings.setOnClickListener(new BackSettingsOnClickListener());
        }
        if (relateBackSetings != null) {
            relateBackSetings.setOnClickListener(new BackSettingsOnClickListener());
        }
    }

    @Override
    protected void onCreate(Bundle savedState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedState);

        mInitialView = null;
        mFinalView = null;
        mInflater = LayoutInflater.from(this);
        mLockUtils = new LockPatternUtils(this);

        establishInitialState();

        new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                int status = intent.getIntExtra("status", 0);
                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                    batteryBgResourceID = R.drawable.battery7;
                } else {
                    batteryBgResourceID = getChargingIcon((level * 100) / scale);
                }
                imgBatteryView.setBackgroundResource(batteryBgResourceID);
            }
        };
    }

    private class BackSettingsOnClickListener implements View.OnClickListener {
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setClass(MasterClear.this, Settings.class);
            startActivity(intent);
            System.out.println("+shy settings masterClear-++++++++++++++++=");
            finish();
        }
    }

    /** Abandon all progress through the confirmation sequence by returning
     * to the initial view any time the activity is interrupted (e.g. by
     * idle timeout).
     */
    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mBatteryInfoReceiver);
        System.out.println("+shy settings onPause-++++++++++isFinishing+=" + isFinishing());
        if (!isFinishing()) {
            establishFinalConfirmationState();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }
}
