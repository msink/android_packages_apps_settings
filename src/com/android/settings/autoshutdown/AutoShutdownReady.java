package com.android.settings.autoshutdown;

import android.app.ActivityManagerNative;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;

public class AutoShutdownReady extends BroadcastReceiver {

    static WakeLock createPartialWakeLock(Context context) {
        PowerManager pm =
            (PowerManager) context.getSystemService(Context.POWER_SERVICE);
       	return pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AlarmAlertWakeLock");
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (!Environment.getFlashStorageState().equals("shared") &&
           		((AutoShutdownService.isShutdown) &&
           		(Settings.System.getInt(context.getContentResolver(),
           			Settings.System.AUTO_SHUTDOWN_TIMEOUT, 0) > 0))) {

        	final WakeLock wl = createPartialWakeLock(context);
            wl.acquire();

	        if (ActivityManagerNative.isSystemReady()) {
    	        Intent shutdown = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
        	    shutdown.putExtra(Intent.EXTRA_KEY_CONFIRM, 0);
	            shutdown.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	        context.startActivity(shutdown);
	        }

            wl.release();
        }
    }
}
