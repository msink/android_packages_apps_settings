package com.android.settings;

import android.content.Context;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ServiceManager;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.provider.Settings;
import android.view.IWindowManager;
import android.widget.Toast;
import android.util.Log;

import java.io.*;

public class HdmiSettings extends PreferenceActivity
    implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "HdmiSettings";
    private static final String KEY_HDMI_RESOLUTION = "hdmi_resolution";
    private static final String KEY_HDMI_MODE = "hdmi_mode";
    private static final String KEY_HDMI_HDCP = "hdmi_hdcp";
    private static final String KEY_HDMI = "hdmi";
    private static final String KEY_HDCP = "HDCP_Setting";

    private boolean IsHdmiFileThere = false;
    private boolean IsHdmiPlug = false;
    private boolean IsHdmiDisplayOn = false;

    private CheckBoxPreference mHdmi;
    private CheckBoxPreference mHdcp;
    private ListPreference mHdmiResolution;
    private ListPreference mHdmiMode;

    private File HdmiFile = null;
    private File HdmiState = null;
    private IWindowManager mWindowManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContentResolver resolver = getContentResolver();
        mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        HdmiFile = new File("/sys/class/hdmi/hdmi-0/enable");
        HdmiState = new File("/sys/class/hdmi/hdmi-0/state");
        addPreferencesFromResource(R.xml.hdmi_settings);
        mHdmi = (CheckBoxPreference)findPreference(KEY_HDMI);
        mHdmi.setPersistent(false);
        SharedPreferences sharedPreferences = getSharedPreferences("HdmiSettings", Context.MODE_PRIVATE);
        int enable = sharedPreferences.getInt("enable", 0);
        if (enable != 0) {
            mHdmi.setChecked(true);
            setHdmiConfig(HdmiFile, true);
        } else {
            mHdmi.setChecked(false);
            setHdmiConfig(HdmiFile, false);
        }
        mHdcp = (CheckBoxPreference)findPreference(KEY_HDCP);
        mHdcp.setPersistent(false);
        mHdcp.setChecked(false);
        mHdmiResolution = (ListPreference)findPreference(KEY_HDMI_RESOLUTION);
        mHdmiResolution.setOnPreferenceChangeListener(this);
        mHdmiMode = (ListPreference)findPreference(KEY_HDMI_MODE);
        mHdmiMode.setOnPreferenceChangeListener(this);
        if (HdmiFile.exists() && HdmiState.exists()) {
            IsHdmiFileThere = true;
            getPreferenceScreen().removePreference(findPreference(KEY_HDCP));
            getPreferenceScreen().removePreference(findPreference(KEY_HDMI_MODE));
        } else {
            IsHdmiFileThere = false;
        }

    }

    protected void setHdmiConfig(File file, boolean enable) {
        if (file.exists()) {
            try {
                SharedPreferences.Editor editor = getPreferences(0).edit();
                String strChecked = "1";
                String strUnChecked = "0";
                RandomAccessFile rdf = null;
                rdf = new RandomAccessFile(file, "rw");
                if (enable) {
                    rdf.writeBytes(strChecked);
                    editor.putInt("enable", 1);
                } else {
                    rdf.writeBytes(strUnChecked);
                    editor.putInt("enable", 0);
                }
                editor.commit();
            } catch (IOException re) {
                Log.e(TAG, "IO Exception");
            }
        } else {
            Log.i(TAG, "The File " + file + " is not exists");
        }
        return;
    }

    protected void setHdmiOutputStyle(File file, int style, String string) {
        if (file.exists()) {
            try {
                FileReader fread = new FileReader(file);
                BufferedReader buffer = new BufferedReader(fread);
                StringBuffer strbuf = new StringBuffer("");
                String str = null;
                String substr = null;
                SharedPreferences.Editor editor = getPreferences(0).edit();
                if (string.equals(KEY_HDMI_RESOLUTION)) {
                    substr = "resolution";
                    while ((str = buffer.readLine()) != null) {
                        if (str.length() == 12) {
                            String res = str.substring(0, 10);
                            if (substr.equals(res)) {
                                String strValue = String.valueOf(style);
                                String s = substr + "=" + strValue;
                                strbuf.append(s + "\n");
                                editor.putInt("resolution", Integer.parseInt(strValue));
                                editor.commit();
                             } else {
                                strbuf.append(str + "\n");
                             }
                         } else {
                               strbuf.append(str + "\n");
                         }
                    }
                }
                if (string.equals(KEY_HDMI_MODE)) {
                    boolean flag = false;
                    substr = "mode";
                    while ((str = buffer.readLine()) != null) {
                        if (str.length() == 6) {
                            String res = str.substring(0, 4);
                            if (substr.equals(res)) {
                                flag = true;
                                String strValue = String.valueOf(style);
                                String s = substr + "=" + strValue;
                                strbuf.append(s + "\n");
                                editor.putInt("mode", Integer.parseInt(strValue));
                                editor.commit();
                            } else {
                                strbuf.append(str + "\n");
                            }
                        } else {
                            strbuf.append(str + "\n");
                        }
                    }
                    if (!flag) {
                        String s = "mode=0";
                        strbuf.append(s + "\n");
                        editor.putInt("mode", 0);
                        editor.commit();
                    }
                }
                if (string.equals(KEY_HDMI_HDCP)) {
                    substr = "hdcp_on";
                    while ((str = buffer.readLine()) != null) {
                        if (str.length() == 9) {
                            String res = str.substring(0, 7);
                            if (substr.equals(res)) {
                                String strValue = String.valueOf(style);
                                String s = substr + "=" + strValue;
                                strbuf.append(s + "\n");
                                editor.putInt("hdcp_on", Integer.parseInt(strValue));
                                editor.commit();
                             } else {
                                strbuf.append(str + "\n");
                             }
                         } else {
                               strbuf.append(str + "\n");
                         }
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
            Log.i(TAG, "The File " + file + " is not exists");
        }
    }

    protected boolean isHdmiConnected(File file) {
        boolean isConnected = false;
        if (file.exists()) {
            try {
                  FileReader fread = new FileReader(file);
                  BufferedReader buffer = new BufferedReader(fread);
                  String strPlug = "plug=1";
                  String str = null;

                  while ((str = buffer.readLine()) != null) {
                    int length = str.length();
                    if (length == 6 && str.equals(strPlug)) {
                        isConnected = true;
                        break;
                    } else {
                        isConnected = false;
                    }
                  }
            } catch (IOException e) {
                Log.e(TAG, "IO Exception");
            }
        }
        return isConnected;
    }

    protected void onResume() {
        super.onResume();

        if (IsHdmiFileThere) {
            SharedPreferences sharedPreferences = getSharedPreferences("HdmiSettings", Context.MODE_PRIVATE);
            int enable = sharedPreferences.getInt("enable", 0);
            int resolution = sharedPreferences.getInt("resolution", 0);
            boolean isConnected = isHdmiConnected(HdmiState);
            Log.d(TAG, "onResume() ==>> isConnected=" + isConnected);
            if (isConnected) {
                if (enable != 0) {
                    mHdmi.setChecked(true);
                    setHdmiConfig(HdmiFile, true);
                    Settings.System.putInt(getContentResolver(), "accelerometer_rotation", 0);
                    Settings.System.putInt(getContentResolver(), "screen_off_timeout", -1);
                } else {
                    Settings.System.putInt(getContentResolver(), "screen_off_timeout", -1);
                }
            } else {
                mHdmi.setChecked(false);
                setHdmiConfig(HdmiFile, false);
                SharedPreferences mPreferences = getPreferenceScreen().getSharedPreferences();
                int mFlag = mPreferences.getInt("mAccelerometer", 1);
                if (mFlag != 0) {
                    Settings.System.putInt(getContentResolver(), "accelerometer_rotation", 1);
                }
            }
            mHdmiResolution.setValue(String.valueOf(resolution));
        }
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (IsHdmiFileThere) {
            SharedPreferences mPreferences = getPreferenceScreen().getSharedPreferences();
            SharedPreferences sharedPrefs = getSharedPreferences("HdmiSettings", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            boolean isConnected = isHdmiConnected(HdmiState);
            int timeout = mPreferences.getInt("timeout", 30000);

            Log.d(TAG, "onPreferenceThreeClick() ==>> isConnected=" + isConnected);
            if (preference == mHdmi) {
                if (mHdmi.isChecked()) {
                    if (isConnected) {
                        setHdmiConfig(HdmiFile, true);
                        mHdmi.setChecked(true);
                        editor.putInt("enable", 1);
                        Settings.System.putInt(getContentResolver(), "accelerometer_rotation", 0);
                        Settings.System.putInt(getContentResolver(), "screen_off_timeout", -1);
                    } else {
                        Log.d("---->>", "cannot open hdmi");
                        mHdmi.setChecked(false);
                        setHdmiConfig(HdmiFile, false);
                        editor.putInt("enable", 0);
                        Toast.makeText(this, getString(R.string.hdmi_notice), 0).show();
                        Settings.System.putInt(getContentResolver(), "screen_off_timeout", timeout);
                    }
                } else {
                    setHdmiConfig(HdmiFile, false);
                    mHdmi.setChecked(false);
                    editor.putInt("enable", 0);
                    Settings.System.putInt(getContentResolver(), "screen_off_timeout", timeout);
                    int mFlag = mPreferences.getInt("mAccelerometer", 0);
                    if (mFlag != 0) {
                        Settings.System.putInt(getContentResolver(), "accelerometer_rotation", 1);
                    }
                }
                editor.commit();
            } else if (preference == mHdcp) {
                String strHdcp = KEY_HDMI_HDCP;
                if (mHdcp.isChecked()) {
                    setHdmiOutputStyle(HdmiState, 1, strHdcp);
                    mHdcp.setChecked(false);
                } else {
                    setHdmiOutputStyle(HdmiState, 0, strHdcp);
                    mHdcp.setChecked(false);
                }
                //editor.commit();
            }
        }
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (IsHdmiFileThere) {
            SharedPreferences.Editor editor = getPreferences(0).edit();
            final String key = preference.getKey();
            if (KEY_HDMI_RESOLUTION.equals(key)){
                try {
                    String strResolution = "hdmi_resolution";
                    int value = Integer.parseInt((String) objValue);
                    editor.putInt("resolution", value);
                    setHdmiOutputStyle(HdmiState, value, strResolution);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "onPreferenceChanged hdmi_resolution setting error");
                }
            }
            if (KEY_HDMI_MODE.equals(key)) {
                try {
                    String strMode = "hdmi_mode";
                    int value = Integer.parseInt((String) objValue);
                    editor.putInt("mode", value);
                    setHdmiOutputStyle(HdmiState, value, strMode);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "onPreferenceChanged hdmi_mode setting error");
                }
            }
            editor.commit();
        }
        return true;
    }
}
