package com.android.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.preference.ListPreference;
import android.view.View;
import android.widget.RelativeLayout;
import android.util.AttributeSet;

public class MyListPreference extends ListPreference {
    private CharSequence[] entries;
    private CharSequence[] entryValues;
    private int indexOfValue;

    public MyListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.listpreference_icon);
    }

    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        super.onSetInitialValue(restoreValue, defaultValue);
        entries = getEntries();
        entryValues = getEntryValues();
        indexOfValue = findIndexOfValue(getSharedPreferences().getString(getKey(), ""));
        if (indexOfValue >= 0) {
            String key = String.valueOf(entries[indexOfValue]);
            if (key != null) {
                setSummary(key);
            }
        }
    }

    public void onBindView(View view) {
        super.onBindView(view);
    }
}
