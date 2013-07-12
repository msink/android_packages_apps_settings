package com.android.settings.autoshutdown;

import android.content.Context;
import android.os.PowerManager;

class AlarmAlertWakeLock {
    static PowerManager.WakeLock createPartialWakeLock(Context context) {
        PowerManager pm =
                (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AlarmAlertWakeLock");
    }
}
