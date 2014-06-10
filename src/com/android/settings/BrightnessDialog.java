package com.android.settings;

import android.app.Dialog;
import android.content.Context;
import android.hardware.DeviceController;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ToggleButton;
import android.widget.RatingBar;

public class BrightnessDialog extends Dialog {

    final private static int LOW_BRIGHTNESS_MAX = Math.min(20, 255);
    private RatingBar mRatingBarLightSettings = null;
    private DeviceController mDeviceController = null;
    private ImageButton mLightAdd;
    private ImageButton mLightDown;
    private ToggleButton mLightSwitch;

    public BrightnessDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_brightness);
        mDeviceController = new DeviceController(context);
        mRatingBarLightSettings = (RatingBar)findViewById(R.id.ratingbar_light_settings);
        mRatingBarLightSettings.setFocusable(false);
        mRatingBarLightSettings.setOnRatingBarChangeListener(
            new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating,
                    boolean fromUser) {
                setLightRatingBarProgress();
            }
        });

        assert(mRatingBarLightSettings.getNumStars() >= 10);

        setLightRatingBarDefaultProgress();

        mLightDown = (ImageButton)findViewById(R.id.imagebutton_light_down);
        mLightDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRatingBarLightSettings.setProgress(
                    mRatingBarLightSettings.getProgress() - 1);
                if (mRatingBarLightSettings.getProgress() == 0) {
                    mLightSwitch.setChecked(false);
                }
            }
        });

        mLightAdd = (ImageButton)findViewById(R.id.imagebutton_light_add);
        mLightAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLightSwitch != null && !mLightSwitch.isChecked()) {
                    mLightSwitch.setChecked(true);
                }
                if (mRatingBarLightSettings.getProgress() ==
                        mRatingBarLightSettings.getMax()) {
                    setLightRatingBarProgress();
                    return;
                }
                mRatingBarLightSettings.setProgress(
                    mRatingBarLightSettings.getProgress() + 1);
            }
        });

        mLightSwitch = (ToggleButton)findViewById(R.id.togglebutton_light_switch);
        mLightSwitch.setOnCheckedChangeListener(
            new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mDeviceController.openFrontLight();
                } else {
                    mDeviceController.closeFrontLight();
                }
            }
        });

        if (mDeviceController.getFrontLightValue() > 0) {
            mLightSwitch.setChecked(true);
        } else {
            mLightSwitch.setChecked(false);
        }
        setCanceledOnTouchOutside(true);
    }

    private void setLightRatingBarDefaultProgress() {
        int value = 0;
        try {
            value = Settings.System.getInt(
                        getContext().getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException snfe) {
            value = mDeviceController.BRIGHTNESS_DEFAULT;
        }
        int rating = getRatingOfBrightness(value);
        mRatingBarLightSettings.setProgress(rating);
    }

    private void setLightRatingBarProgress() {
        int value = getBrightnessOfRating(
                mRatingBarLightSettings.getProgress());
        Settings.System.putInt(
                getContext().getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS,
                value);
        mDeviceController.setFrontLightValue(value);
    }

    private int getBrightnessOfRating(int value) {
        if (value <= 10)
            return (value * 2) + mDeviceController.BRIGHTNESS_MINIMUM;
        int big_interval = (mDeviceController.BRIGHTNESS_MAXIMUM - LOW_BRIGHTNESS_MAX)
                         / (mRatingBarLightSettings.getNumStars() - 10);
        return ((value - 10) * big_interval) + LOW_BRIGHTNESS_MAX;
    }

    private int getRatingOfBrightness(int value){
        if (value <= LOW_BRIGHTNESS_MAX)
            return (value - mDeviceController.BRIGHTNESS_MINIMUM) / 2;

        int big_interval = (mDeviceController.BRIGHTNESS_MAXIMUM - LOW_BRIGHTNESS_MAX)
                         / (mRatingBarLightSettings.getNumStars() - 10);
        return ((value - LOW_BRIGHTNESS_MAX) / big_interval) + 10;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_LEFT:
            if (event.getRepeatCount() == 0) {
                if (!mLightDown.isFocused()) {
                    mLightDown.requestFocus();
                }
                event.startTracking();
                return true;
            }
            break;
        case KeyEvent.KEYCODE_DPAD_RIGHT:
            if (event.getRepeatCount() == 0) {
                if (!mLightAdd.isFocused()) {
                    mLightAdd.requestFocus();
                }
                event.startTracking();
                return true;
            }
            break;
        }
        return super.onKeyDown(keyCode, event);
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
                setLightRatingBarProgress();
            } else {
                mRatingBarLightSettings.setProgress(
                    mRatingBarLightSettings.getProgress() + 1);
            }
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }
}
