package com.android.settings;

import android.content.Context;
import android.hardware.DeviceController;

public class DeviceControllerHelper {
    private DeviceController deviceController = null;
    private static DeviceControllerHelper sInstance = null;

    public static DeviceControllerHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DeviceControllerHelper(context);
        }
        return sInstance;
    }

    private DeviceControllerHelper(Context context) {
        deviceController = new DeviceController(context);
    }

    public DeviceController getDeviceController() {
        return deviceController;
    }
}
