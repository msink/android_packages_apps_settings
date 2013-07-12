package com.android.settings.autoshutdown;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.util.Log;

public class AutoShutdownReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.e("AutoShutdown", "ready to shutdown device");
        final WakeLock wl = AlarmAlertWakeLock.createPartialWakeLock(context);
        wl.acquire();

        Intent alert = new Intent();
        alert.setClass(context, AutoShutdownAlertDialog.class);
        alert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(alert);
        wl.release();
    }
}
