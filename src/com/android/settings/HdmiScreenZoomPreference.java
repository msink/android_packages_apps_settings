package com.android.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.SeekBarPreference;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.util.AttributeSet;
import android.util.Log;

import java.io.*;
import java.util.Map;

public class HdmiScreenZoomPreference extends SeekBarPreference implements
    SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "HdmiScreenZoomPreference";

    private File HdmiState = new File("/sys/class/hdmi/hdmi-0/state");
    private SeekBar mSeekBar;
    private int mOldScale = 0;
    private int mValue = 0;
    private int mRestoreValue = 0;
    private boolean mFlag  = false;
    private Context context;

    public HdmiScreenZoomPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setDialogLayoutResource(R.layout.preference_dialog_screen_scale);
        setDialogIcon(R.drawable.ic_settings_screen_scale);
    }

    protected void setHdmiScreenScale(File file, int value) {
        if (file.exists()) {
            try {
                FileReader fread = new FileReader(file);
                BufferedReader buffer = new BufferedReader(fread);
                StringBuffer strbuf = new StringBuffer("");
                String substr = "scale_set";
                String str = null;
                int length = 0;
                while ((str = buffer.readLine()) != null) {
                    length = str.length();
                    if (length == 13 || length == 12) {
                        String res = str.substring(0, 9);
                        if (substr.equals(res)) {
                            String strValue = String.valueOf(value);
                            String s = substr + "=" + strValue;
                            strbuf.append(s + "\n");
                         } else {
                            strbuf.append(str + "\n");
                         }
                    } else {
                            strbuf.append(str + "\n");
                    }
                }
                buffer.close();
                fread.close();
                File f = new File("/sys/class/hdmi/hdmi-0/state");
                OutputStream output = null;
                OutputStreamWriter outputWrite = null;
                PrintWriter print = null;
                try {
                    output = new FileOutputStream(f);
                    outputWrite = new OutputStreamWriter(output);
                    print = new PrintWriter(outputWrite);
                    print.print(strbuf.toString());
                    print.flush();
                    output.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                Log.e(TAG, "IO Exception");
            }
        } else {
            Log.e(TAG, "File:" + file + "not exists");
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mFlag = false;
        mSeekBar = getSeekBar(view);
        SharedPreferences preferences = context.getSharedPreferences("HdmiSettings", context.MODE_PRIVATE);
        mOldScale = preferences.getInt("scale_set", 100);
        mOldScale = mOldScale - 80;

        mSeekBar.setProgress(mOldScale);
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        mValue = progress + 80;
        if (mValue > 100) {
            mValue = 100;
        }
        setHdmiScreenScale(HdmiState, mValue);
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        mFlag = true;
        mRestoreValue = seekBar.getProgress();
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        setHdmiScreenScale(HdmiState, mValue);
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        SharedPreferences preferences = context.getSharedPreferences("HdmiSettings", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if (positiveResult) {
            int value = mSeekBar.getProgress() + 80;
            setHdmiScreenScale(HdmiState, value);
            editor.putInt("scale_set", value);
        } else {
            if (mFlag) {
                mRestoreValue = mRestoreValue + 80;
                if (mRestoreValue > 100) {
                    mRestoreValue = 100;
                }
                setHdmiScreenScale(HdmiState, mRestoreValue);
                editor.putInt("scale_set", mRestoreValue);
            } else {
                int value = mSeekBar.getProgress() + 80;
                setHdmiScreenScale(HdmiState, value);
                editor.putInt("scale_set", value);
            }
        }
        editor.commit();
    }
}
