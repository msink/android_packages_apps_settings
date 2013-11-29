package com.android.settings;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.util.Log;

public class memoryLowIndicationReciver extends BroadcastReceiver {
    private String TAG = "memoryLowIndicationReciver";
    private Context mContext;
    private long availableSize;

    public void onReceive(Context context, Intent intent) {
        mContext = context;
        Uri uri = intent.getData();
        String action = intent.getAction();
        Log.d(TAG, action);
        if (uri.getScheme().equals("file")
            && action.equals(Intent.ACTION_MEDIA_MOUNTED)
            && !checkInternelMemory()) {
            showNotification();
        }
    }

    private void showNotification() {
        String title = mContext.getString(R.string.memory_low);
        String msg = this.mContext.getString(R.string.memory_low1);
        NotificationManager manager = (NotificationManager)mContext.getSystemService("notification");
        Notification mNotification = new Notification(
            R.drawable.ic_settings_about, title, System.currentTimeMillis());
        Intent openintent = new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, openintent, 0);
        mNotification.setLatestEventInfo(mContext, title, msg, contentIntent);
        mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
        mNotification.defaults |= Notification.DEFAULT_SOUND;
        manager.notify(1, mNotification);
    }

    private boolean checkInternelMemory() {
        String path = Environment.getFlashStorageDirectory().getPath();
      String state = Environment.getExternalStorageState();
      if (Environment.MEDIA_MOUNTED.equals(state)) {
        StatFs stat = new android.os.StatFs(path);
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        availableSize = availableBlocks * blockSize / 0x100000;
        Log.d(TAG, "availableSize=" + availableSize + "MB");
        Log.d(TAG, "path=" + path + "");
        if (availableSize < 10) {
            return false;
        }
      }
        return true;
    }
}
