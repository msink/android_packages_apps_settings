package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;

public class Help {

    private static Timer mTimer = null;
    private static TimerTask mTimerTask = null;

    private static InfoService inforSer;
    private static TitlePreference tiltPreference;

    public static void startInforService(Context context) {
        inforSer = new InfoService();
        context.startService(new Intent(context, InfoService.class));
    }

    public static void stopInforService(Context context) {
        context.stopService(new Intent(context, InfoService.class));
    }

    public static void startTimerChangeBattery(TitlePreference titlepre) {
        mTimer = new Timer();
        tiltPreference = titlepre;
        setInfoBattery(tiltPreference);
        mTimerTask = new TimerTask() {
            public void run() {
                handler.post(runnableInfoUi);
            }
        };
        mTimer.schedule(mTimerTask, 2000, 2000);
    }

    static Handler handler = new Handler();
    static Runnable runnableInfoUi = new Runnable() {
        public void run() {
            setInfoBattery(tiltPreference);
        }
    };

    public static void setInfoBattery(TitlePreference titPre) {
        if (InfoService.batteryBgResourceID != -1 && titPre != null) {
            titPre.setmBgResuID(InfoService.batteryBgResourceID);
        }
    }

    public static void stopTimer() {
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimer.cancel();
            mTimerTask = null;
        }
    }
}
