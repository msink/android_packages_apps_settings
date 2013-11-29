package com.android.settings;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

public class SummaryListAdapter extends ArrayAdapter<CharSequence> {
    private int index = 0;
    private String[] s = null;

    public SummaryListAdapter(Context context, int textViewResourceId,
            CharSequence[] objects, String[] ids, int i) {
        super(context, textViewResourceId, objects);
        index = i;
        s = ids;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        View row = inflater.inflate(R.layout.keybord_mapping_preference_layout, parent, false);
        TextView textView = (TextView)row.findViewById(R.id.summary);
        textView.setText(s[position]);
        CheckedTextView checkedTextView = (CheckedTextView)row.findViewById(R.id.check);
        checkedTextView.setText(this.getItem(position));
        if (position == index) {
            checkedTextView.setChecked(true);
        }
        return row;
    }
}
