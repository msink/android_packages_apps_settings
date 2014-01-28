package com.android.settings.autoshutdown;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;

public class AutoShutdownService extends Service {

    private AlarmManager am;
    public static boolean isShutdown;
    private PendingIntent sender;

    private void EnableAutoShutdown(int timeout) {
        am.set(AlarmManager.RTC_WAKEUP,	System.currentTimeMillis() + timeout, sender);
    }

    private void DisableAutoShutdown(){
        am.cancel(sender);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent("COM.CARATION.AUTO_SHUTDOWN_READY");
        am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        sender = PendingIntent.getBroadcast(this,
        	0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        isShutdown = intent.getBooleanExtra("IS_SHUTDOWN", false);
        int timeout = Settings.System.getInt(getContentResolver(),
        	Settings.System.AUTO_SHUTDOWN_TIMEOUT, 0);
        if (isShutdown && timeout > 0) {
            DisableAutoShutdown();
            EnableAutoShutdown(timeout);
        } else {
            DisableAutoShutdown();
            onDestroy();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // not supported
        return null;
    }
}
