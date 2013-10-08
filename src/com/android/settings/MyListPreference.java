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
    private Drawable mBg;
    private Context cxt;

    public MyListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.listpreference_icon);
        cxt = context;
        TypedArray a = context.obtainStyledAttributes(attrs,
                 R.styleable.MyIconPreferenceScreen, 0, 0);
        mBg = a.getDrawable(1);
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
        view.setBackgroundColor(0);
        RelativeLayout relatePreference = (RelativeLayout)
            view.findViewById(R.id.relate_listpreference);
        if (relatePreference != null && mBg != null) {
            relatePreference.setBackgroundDrawable(mBg);
        }
    }

    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
    }
}
