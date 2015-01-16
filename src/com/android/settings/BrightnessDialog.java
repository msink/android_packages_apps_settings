package com.android.settings;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.DeviceController;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ToggleButton;
import android.widget.RatingBar;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BrightnessDialog extends Dialog {

    public static int BRIGHTNESS_ON = DeviceController.BRIGHTNESS_MAXIMUM;
    private RatingBar mRatingBarLightSettings = null;
    private DeviceController mDev = null;

    private Integer[] mFrontLightValue = {
          0,   5,  10,  15,  20,  25,  30,  35,  40,  45,
         50,  55,  60,  65,  70,  75,  80,  85,  90,  95,
        100, 105, 110, 115, 120, 125, 130, 135, 140, 145,
        150, 155, 160,           175, 180, 185, 190, 195,
        200, 205, 210, 215, 220, 225, 230, 235, 240, 245,
        250 };

    private boolean isLongClickOpenAndCloseFrontLight = false;
    private BroadcastReceiver mOpenAndCloseFrontLightReceiver = null;
    private IntentFilter filter = null;
    private Context mContext = null;
    private List<Integer> mLightSteps = new ArrayList();
    private ToggleButton mLightSwitch;

    private List<Integer> initRangeArray(int numStarts) {
        List<Integer> brightnessList = new ArrayList(numStarts);
        for (int i = 0; i <= numStarts; i++) {
            brightnessList.add((BRIGHTNESS_ON * i) / numStarts);
        }
        return brightnessList;
    }

    public BrightnessDialog(Context context) {
        super(context);
        mContext = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_brightness);
        mDev = new DeviceController(mContext);
        mRatingBarLightSettings = (RatingBar)findViewById(R.id.ratingbar_light_settings);
        mRatingBarLightSettings.setFocusable(false);

        mRatingBarLightSettings.setOnRatingBarChangeListener(
            new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating,
                    boolean fromUser) {
                if (!isLongClickOpenAndCloseFrontLight) {
                    setFrontLightValue();
                }
                updateLightSwitch();
                isLongClickOpenAndCloseFrontLight = false;
            }

        });

        mOpenAndCloseFrontLightReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null &&
                        (DeviceController.ACTION_OPEN_FRONT_LIGHT.equals(intent.getAction()) ||
                        DeviceController.ACTION_CLOSE_FRONT_LIGHT.equals(intent.getAction()))) {
                    isLongClickOpenAndCloseFrontLight = true;
                    int front_light_value = intent.getIntExtra(DeviceController.INTENT_FRONT_LIGHT_VALUE, 0);
                    mRatingBarLightSettings.setProgress(getIndex(front_light_value));
                }
            }
        };
        filter = new IntentFilter();
        filter.addAction(DeviceController.ACTION_OPEN_FRONT_LIGHT);
        filter.addAction(DeviceController.ACTION_CLOSE_FRONT_LIGHT);
        mContext.registerReceiver(mOpenAndCloseFrontLightReceiver, filter);

        mLightSteps = Arrays.asList(mFrontLightValue);
        if (mLightSteps != null) {
            mRatingBarLightSettings.setNumStars(mLightSteps.size() - 1);
            mRatingBarLightSettings.setMax(mLightSteps.size() - 1);
        } else {
            int numStarts = mRatingBarLightSettings.getNumStars();
            mLightSteps = initRangeArray(numStarts);
        }
        Collections.sort(mLightSteps);

        setLightRatingBarDefaultProgress();

        ImageButton mLightDown = (ImageButton) findViewById(R.id.imagebutton_light_down);
        mLightDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRatingBarLightSettings.setProgress(mRatingBarLightSettings.getProgress() - 1);
            }
        });

        ImageButton mLightAdd = (ImageButton) findViewById(R.id.imagebutton_light_add);
        mLightAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRatingBarLightSettings.getProgress() == mRatingBarLightSettings.getMax()) {
                    setFrontLightValue();
                } else {
                    mRatingBarLightSettings.setProgress(mRatingBarLightSettings.getProgress() + 1);
                }
            }
        });

        mLightSwitch = (ToggleButton) findViewById(R.id.togglebutton_light_switch);
        mLightSwitch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (mLightSwitch.isChecked()) {
                    mDev.closeFrontLight();
                } else {
                    mDev.openFrontLight();
                }
            }
        });

        if (isLightOn(getContext())) {
            mLightSwitch.setChecked(true);
        } else {
            mLightSwitch.setChecked(false);
        }

        setCanceledOnTouchOutside(true);
    }

    private void updateLightSwitch() {
        if (mLightSwitch != null) {
            if (mRatingBarLightSettings.getProgress() != 0 && !mLightSwitch.isChecked()) {
                mLightSwitch.setChecked(true);
            } else if (mRatingBarLightSettings.getProgress() == 0 && mLightSwitch.isChecked()) {
                mLightSwitch.setChecked(false);
            }
        }
    }

    private void setLightRatingBarDefaultProgress() {
        int value = getFrontLightConfigValue(getContext());
        mRatingBarLightSettings.setProgress(getIndex(value));
    }

    private boolean isLightOn(Context context) {
        return (mDev.getFrontLightValue() > 0);
    }

    private void setFrontLightValue() {
        if (mLightSteps.size() <= 0) return;
        int value = ((Integer)(mLightSteps.get(mRatingBarLightSettings
                                          .getProgress()))).intValue();
        mDev.setFrontLightValue(value);
        setFrontLightConfigValue(mContext, value);
    }

    public int getFrontLightConfigValue(Context context) {
        int res = 0;
        int light_value;
        try {
            light_value = Settings.System.getInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException snfe) {
            light_value = DeviceController.BRIGHTNESS_DEFAULT;
        }
        res = light_value;
        return res;
    }

    public boolean setFrontLightConfigValue(Context context, int value) {
        return Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, value);
    }

    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_LEFT:
            mRatingBarLightSettings.setProgress(
                mRatingBarLightSettings.getProgress() - 1);
            return true;
        case KeyEvent.KEYCODE_DPAD_RIGHT:
            if (mRatingBarLightSettings.getProgress() ==
                    mRatingBarLightSettings.getMax()) {
                setFrontLightValue();
            } else {
                mRatingBarLightSettings.setProgress(
                    mRatingBarLightSettings.getProgress() + 1);
            }
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    public void cancel() {
        super.cancel();
        if (mOpenAndCloseFrontLightReceiver != null) {
            mContext.unregisterReceiver(mOpenAndCloseFrontLightReceiver);
        }
    }

    private int getIndex(int val) {
        int index = Collections.binarySearch(mLightSteps, val);
        if (index == -1) {
            index = 0;
        } else if (index < 0) {
            if (Math.abs(index) <= mLightSteps.size()) {
                index = Math.abs(index) - 2;
            } else {
                index = mLightSteps.size() - 1;
            }
        }
        return index;
    }
}
