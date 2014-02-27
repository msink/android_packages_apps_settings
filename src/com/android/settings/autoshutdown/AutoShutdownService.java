package com.android.settings.autoshutdown;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class AutoShutdownService extends Service {

    public static boolean isShutdown;
    private AlarmManager am;
    private PendingIntent sender;

    private void EnableAutoShutdown(int timeout) {
        am.set(AlarmManager.RTC_WAKEUP,
               System.currentTimeMillis() + timeout, sender);
    }

    private void DisenableAutoShutdown(){
        am.cancel(sender);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent("COM.CARATION.AUTO_SHUTDOWN_RECEIVER");
        am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        sender = PendingIntent.getBroadcast(this,
                 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Log.e("AutoShutdown", "Service");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        isShutdown = intent.getBooleanExtra("IS_SHUTDOWN", false);
        int time = -1;
        try {
            time = Settings.System.getInt(getContentResolver(),
                   Settings.System.AUTO_SHUTDOWN_TIMEOUT);
            Log.e("AutoShutdown", "time:" + time);
            Log.e("AutoShutdown", "isShutdown:" + isShutdown);
        } catch (Exception e) {}

        if (isShutdown && time > 0) {
            DisenableAutoShutdown();
            EnableAutoShutdown(time);
            Log.e("AutoShutdown", "isShutdowntime:" + isShutdown);
        } else {
            DisenableAutoShutdown();
            onDestroy();
            Log.e("AutoShutdown", "DisenableAutoShutdown");
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
