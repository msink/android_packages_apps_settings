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
        this.mSntpHandler.post(this.mSntpTask);
    }

    public boolean syncSntp() {
        SntpClient client = new SntpClient();
        if (client.requestTime("ntp.sjtu.edu.cn", 30000)) {
            long now = client.getNtpTime() + SystemClock.elapsedRealtime() - client.getNtpTimeReference();
            CharSequence ch = DateFormat.format("hh:mm:ss", now);
            Log.d(TAG, "time=" + ch);
            CharSequence date = DateFormat.format("yyyy MM dd", now);
            Log.d(TAG, "date=" + date);
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
}
