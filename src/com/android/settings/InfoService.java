package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class InfoService extends android.app.Service {

    public static int wifiBgResourceID = -1;
    private int wifiStrength = 0;

    private boolean wifiState = false;
    private boolean mobile3GState = false;
    private int BatteryN = 0;
    boolean flag;

    public static int batteryBgResourceID = -1;
    public static int batteryDianLiang = 0;
    public static String resBattName = "";

    public IBinder onBind(Intent intent) {
        return null;
    }

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            flag = true;
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                updateConnectivity(intent);
            }
        }
    };

    private void obtainWifiInfo() {
        WifiManager wifiManager = (WifiManager) getSystemService("wifi");
        WifiInfo info = wifiManager.getConnectionInfo();
        if (info.getBSSID() != null) {
            int strength = WifiManager.calculateSignalLevel(info.getRssi(), 5);
            int speed = info.getLinkSpeed();
            String units = "Mbps";
            String ssid = info.getSSID();
            wifiStrength = strength;
            switch (wifiStrength) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            default:
                return;
            }
        }
    }

    private void updateConnectivity(Intent intent) {
        NetworkInfo info = (NetworkInfo)(intent.getParcelableExtra(
            ConnectivityManager.EXTRA_NETWORK_INFO));
        switch (info.getType()) {
        case 1:
            if (info.isConnected()) {
                wifiState = true;
                obtainWifiInfo();
            } else {
                wifiState = false;
                wifiBgResourceID = -1;
            }
            break;
        case 0:
            if (info.isConnected()) {
                mobile3GState = true;
            } else {
                mobile3GState = false;
            }
            break;
        default:
            wifiBgResourceID = -1;
        }
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

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int level = intent.getIntExtra("level", 0);
            int scale = intent.getIntExtra("scale", 100);
            int status = intent.getIntExtra("status", 0);
            flag = true;
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                switch (status) {
                case BatteryManager.BATTERY_STATUS_UNKNOWN:
                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                default:
                    BatteryN = (level * 100) / scale;
                    resBattName = "stat_sys_battery_ other";
                    batteryBgResourceID = getChargingIcon(BatteryN);
                    batteryDianLiang = (level * 100) / scale;
                    break;
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    resBattName = "stat_sys_battery_CHARGING";
                    batteryBgResourceID = R.drawable.battery7;
                    batteryDianLiang = (level * 100) / scale;
                    break;
                case BatteryManager.BATTERY_STATUS_FULL:
                    resBattName = "stat_sys_battery_100";
                    batteryDianLiang = (level * 100) / scale;
                    batteryBgResourceID = R.drawable.battery1;
                    break;
                }
            }
        }
    };

    public void onCreate() {
    }

    public void onDestroy() {
        stopSelf();
        unregisterReceiver(mBatInfoReceiver);
        unregisterReceiver(wifiReceiver);
    }

    public void onStart(Intent intent, int startid) {
        new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                int status = intent.getIntExtra("status", 0);
                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                    batteryBgResourceID = R.drawable.battery7;
                    batteryDianLiang = (level * 100) / scale;
                } else {
                    batteryBgResourceID = getChargingIcon((level * 100) / scale);
                    batteryDianLiang = (level * 100) / scale;
                }
            }
        };
        new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                flag = true;
                if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    updateConnectivity(intent);
                }
            }
        };
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter filterWifi = new IntentFilter();
        filterWifi.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filterWifi.addAction(WifiManager.RSSI_CHANGED_ACTION);
        registerReceiver(wifiReceiver, filterWifi);
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mFilter.addAction(Intent.ACTION_BATTERY_LOW);
        mFilter.addAction(Intent.ACTION_BATTERY_OKAY);
        mFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        registerReceiver(mBatInfoReceiver, mFilter);
        return super.onStartCommand(intent, flags, startId);
    }
}
