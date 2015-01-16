package com.android.settings.parentcontrol.utils;
import com.android.settings.parentcontrol.data.*;
import com.android.settings.parentcontrol.*;
import com.android.settings.R;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.parentcontrol.ParentControl;

public class ParentControlUtil {
    private static ParentControl mParentControl;

    public static String getLockListTextByLockList(Context context, String lockListValue) {
        String lockListText = null;
        if (LockList.LOCK_OPEN_WIFI.toString()
                .equals(lockListValue)) {
            lockListText = context.getResources()
                .getString(R.string.lock_list_open_wifi);
        } else if (LockList.LOCK_CONNECT_USB_STORAGE.toString()
                .equals(lockListValue)) {
            lockListText = context.getResources()
                .getString(R.string.lock_list_connect_usb_storage);
        } else if (LockList.LOCK_ACCESS_EXT_SD_CARD.toString()
               .equals(lockListValue)) {
            lockListText = context.getResources()
                .getString(R.string.lock_list_access_ext_sd_card);
        } else if (LockList.LOCK_FACTORY_DATA_RESET.toString()
                .equals(lockListValue)) {
            lockListText = context.getResources()
                .getString(R.string.lock_list_factory_data_reset);
        }
        return lockListText;
    }

    public static Intent getParentControlSettingsIntent() {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings",
            "com.android.settings.parentcontrol.ParentControlSettings");
        return intent;
    }

    public static Intent getLockTypeIntentByLockList(Context context, String lockListValue) {
        mParentControl = new ParentControl(context);
        String lockType = mParentControl.getLockType(context, lockListValue);
        Intent intent = new Intent(context, PinAndPasswordActivity.class);
        if (LockType.PATTERN.toString().equals(lockType)) {
            intent = new Intent(context, LockPatternActivity.class);
        }
        intent.putExtra("LOCK_LIST", lockListValue);
        intent.putExtra("LOCK_TYPE", lockType);
        return intent;
    }

    public static boolean isNeedInputPassword(Context context, String lockListValue) {
        boolean needInputPassword = true;
        WifiManager wifiManager = null;
        if (LockList.LOCK_OPEN_WIFI.toString().equals(lockListValue)) {
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager.getWifiState() != 1) {
                needInputPassword = false;
            }
        }
        return needInputPassword;
    }
}
