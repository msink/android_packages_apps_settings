package com.android.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.widget.ListAdapter;
import android.util.AttributeSet;

public class SummaryListPreference extends ListPreference {
    private String[] summarys = null;

    public SummaryListPreference(Context context, AttributeSet attr) {
        super(context, attr);
        TypedArray typedArray = context.obtainStyledAttributes(attr, R.styleable.SummaryListPreference);
        String[] summaryTemp = context.getResources().getStringArray(
            typedArray.getResourceId((typedArray.getIndexCount() - 1), -1));
        summarys = summaryTemp;
        typedArray.recycle();
    }

    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        int index = findIndexOfValue(getSharedPreferences().getString(getKey(), "1"));
        ListAdapter listAdapter = new SummaryListAdapter(getContext(),
                R.layout.keybord_mapping_preference_layout,
                getEntries(), summarys, index);
        builder.setAdapter(listAdapter, this);
        super.onPrepareDialogBuilder(builder);
    }
}
