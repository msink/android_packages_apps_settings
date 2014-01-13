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

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.PhoneStateIntentReceiver;
import com.android.internal.telephony.TelephonyProperties;
import com.android.settings.TitlePreference;
import com.android.settings.MyIconPreferenceScreen;
import com.android.settings.ChildTitlePreference;
import com.android.settings.Help;
import com.android.settings.R;

import java.lang.ref.WeakReference;

/**
 * Display the following information
 * # Phone Number
 * # Network
 * # Roaming
 * # Device Id (IMEI in GSM and MEID in CDMA)
 * # Network type
 * # Signal Strength
 * # Battery Strength  : TODO
 * # Uptime
 * # Awake Time
 * # XMPP/buzz/tickle status : TODO
 *
 */
public class Status extends PreferenceActivity {

    private static final String KEY_WIFI_MAC_ADDRESS = "wifi_mac_address";
    private static final String KEY_BT_ADDRESS = "bt_address";
    private static final int EVENT_SIGNAL_STRENGTH_CHANGED = 200;
    private static final int EVENT_SERVICE_STATE_CHANGED = 300;

    private static final int EVENT_UPDATE_STATS = 500;

    private MyIconPreferenceScreen mBatteryStatus;
    private MyIconPreferenceScreen mBatteryLevel;
    private ChildTitlePreference preferenceBackSettings;
    TitlePreference titlePre;

    private static class MyHandler extends Handler {
        private WeakReference<Status> mStatus;

        public MyHandler(Status activity) {
            mStatus = new WeakReference<Status>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            Status status = mStatus.get();
            if (status == null) {
                return;
            }

            switch (msg.what) {
                case EVENT_UPDATE_STATS:
                    sendEmptyMessageDelayed(EVENT_UPDATE_STATS, 1000);
                    break;
            }
        }
    }

    private int BatteryN = 0;
    public int batteryBgResourceID = -1;
    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {

                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                
                mBatteryLevel.setSummary(String.valueOf(level * 100 / scale) + "%");
                
                int plugType = intent.getIntExtra("plugged", 0);
                int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
                String statusString;
                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                    statusString = getString(R.string.battery_info_status_charging);
                    if (plugType > 0) {
                        statusString = statusString + " " + getString(
                                (plugType == BatteryManager.BATTERY_PLUGGED_AC)
                                        ? R.string.battery_info_status_charging_ac
                                        : R.string.battery_info_status_charging_usb);
                    }
                    batteryBgResourceID = R.drawable.battery7;
                } else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
                    BatteryN = (level * 100) / scale;
                    statusString = getString(R.string.battery_info_status_unknown);
                    batteryBgResourceID = getChargingIcon(BatteryN);
                    statusString = getString(R.string.battery_info_status_discharging);
                } else if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
                    BatteryN = (level * 100) / scale;
                    statusString = getString(R.string.battery_info_status_unknown);
                    batteryBgResourceID = getChargingIcon(BatteryN);
                    statusString = getString(R.string.battery_info_status_not_charging);
                } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
                    statusString = getString(R.string.battery_info_status_full);
                    batteryBgResourceID = R.drawable.battery1;
                } else {
                    BatteryN = (level * 100) / scale;
                    statusString = getString(R.string.battery_info_status_unknown);
                    batteryBgResourceID = getChargingIcon(BatteryN);
                }
                if (titlePre != null) {
                    setInfoBattery(titlePre);
                }
                mBatteryStatus.setSummary(statusString);
            }
        }
    };

    @Override
    protected void onCreate(Bundle icicle) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(icicle);
        Help.startInforService(this);
        Preference removablePref;

        addPreferencesFromResource(R.xml.device_info_status);
        mBatteryLevel = (MyIconPreferenceScreen) findPreference("battery_level");
        mBatteryStatus = (MyIconPreferenceScreen) findPreference("battery_status");
        titlePre = (TitlePreference) findPreference("status_title");
        preferenceBackSettings = (ChildTitlePreference) findPreference("satausettings_back");
        
        getListView().setDividerHeight(-1);
        getListView().setDivider(null);

        setWifiStatus();
        setBtStatus();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Help.startTimerChangeBattery(titlePre);

        registerReceiver(mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }
    
    @Override
    public void onPause() {
        super.onPause();
        Help.stopTimer();

        unregisterReceiver(mBatteryInfoReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Help.stopInforService(this);
        Help.stopTimer();
    }

    private void setWifiStatus() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        Preference wifiMacAddressPref = findPreference(KEY_WIFI_MAC_ADDRESS);
        String macAddress = wifiInfo == null ? null : wifiInfo.getMacAddress();
        wifiMacAddressPref.setSummary(!TextUtils.isEmpty(macAddress) ? macAddress 
                : getString(R.string.status_unavailable));
    }

    private void setBtStatus() {
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        Preference btAddressPref = findPreference(KEY_BT_ADDRESS);

        if (bluetooth == null) {
            // device not BT capable
            getPreferenceScreen().removePreference(btAddressPref);
        } else {
            String address = bluetooth.isEnabled() ? bluetooth.getAddress() : null;
            btAddressPref.setSummary(!TextUtils.isEmpty(address) ? address
                    : getString(R.string.status_unavailable));
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == preferenceBackSettings) {
            finish();
        }

        return false;
    }

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

    public void setInfoBattery(TitlePreference titPre) {
        if (batteryBgResourceID != -1 && titPre != null) {
            titPre.setmBgResuID(batteryBgResourceID);
        }
    }
}
