package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.*;

public class HdmiReceiver extends BroadcastReceiver {
    private static final String TAG = "HdmiReceiver";
    private File HdmiState = new File("/sys/class/hdmi/hdmi-0/state");

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            SharedPreferences preferences = context.getSharedPreferences("HdmiSettings", 0);
            int scale = preferences.getInt("scale_set", 100);
            int resol = preferences.getInt("resolution", 0);
            restoreHdmiValue(HdmiState, scale, "hdmi_scale");
            restoreHdmiValue(HdmiState, resol, "hdmi_resolution");
        }
    }

    protected void restoreHdmiValue(File file, int value, String style) {
        if (file.exists()) {
            try {
                FileReader fread = new FileReader(file);
                BufferedReader buffer = new java.io.BufferedReader(fread);
                StringBuffer strbuf = new StringBuffer("");
                String substr = null;
                String str = null;
                int length = 0;

                if (style.equals("hdmi_scale")) {
                    substr = "scale_set";
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
                }

                if (style.equals("hdmi_resolution")) {
                    substr = "resolution";
                    while ((str = buffer.readLine()) != null) {
                        if (str.length() == 12) {
                            String res = str.substring(0, 10);
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
}
