package com.android.settings;

import android.app.Service;
import android.content.Intent;
import android.net.SntpClient;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.Log;

public class Sntp extends Service {
    private static final String TAG = "Sntp";

    private Handler mSntpHandler = new Handler();
    private Runnable mSntpTask = new Runnable() {
        public void run() {
            syncSntp();
        }
    };

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        mSntpHandler.removeCallbacks(mSntpTask);
        Log.d(TAG, " SNTP Service end !! ");
    }

    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(TAG, " start SNTP Service !! ");
        mSntpHandler.removeCallbacks(mSntpTask);
        mSntpHandler.post(mSntpTask);
    }

    public boolean syncSntp() {
        SntpClient client = new SntpClient();
        if (client.requestTime("ntp.sjtu.edu.cn", 30000)) {
            long now = client.getNtpTime() + SystemClock.elapsedRealtime() - client.getNtpTimeReference();
            long systemTime = System.currentTimeMillis();
            printTime(now, "now:");
            printTime(systemTime, "systemTime:");
            if (systemTime / 60000 == now / 60000) {
                Log.d(TAG, "skip set time");
                return true;
            }
            if (SystemClock.setCurrentTimeMillis(now)) {
                Log.d(TAG, "set CurrentTimeMillis to " + System.currentTimeMillis());
            } else {
                Log.d(TAG, " set CurrentTimeMillis failed");
            }
            return true;
        } else {
            Log.d(TAG, "sntp request time failed");
            return false;
        }
    }

    private void printTime(long time, String prefix) {
        CharSequence ch = DateFormat.format("hh:mm:ss", time);
        CharSequence date = DateFormat.format("yyyy MM dd", time);
        Log.d(TAG, prefix + "time=" + ch + ", date=" + date);
    }
}
