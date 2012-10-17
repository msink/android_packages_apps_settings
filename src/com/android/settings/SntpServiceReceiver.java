package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;

public class SntpServiceReceiver extends BroadcastReceiver {
    private static final String TAG = "SntpServiceReceiver";

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService("connectivity");
            if (cm != null && cm.getActiveNetworkInfo() != null) {
                NetworkInfo.State state = cm.getActiveNetworkInfo().getState();
                Log.i(TAG, "state===============" + state);
                if (NetworkInfo.State.CONNECTED == state && getAutoState(context)) {
                    Log.i(TAG, "starting service");
                    context.startService(new Intent(context, Sntp.class));
                }
            }
        }
    }

    private boolean getAutoState(Context context) {
        try {
            return Settings.System.getInt(context.getContentResolver(),
                   Settings.System.AUTO_TIME) > 0;
        } catch (Settings.SettingNotFoundException snfe) {
            return false;
        }
    }
}
