package com.android.settings.autoshutdown;

import com.android.settings.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RestoreDialogAdapter extends BaseAdapter {
    private String[] items;
    private Context mContext;

    public RestoreDialogAdapter(Context context, String[] items) {
        this.mContext = context;
        this.items = items;
    }

    class MyHolder {
        TextView tv;
    }

    public int getCount() {
        return items.length;
    }

    public Object getItem(int position) {
        return items[position];
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        MyHolder myHolder = null;
        if (convertView == null) {
            myHolder = new MyHolder();
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.dialog_list_item, null);
            myHolder.tv = (TextView) convertView.findViewById(R.id.dialog_item_label);
            convertView.setTag(myHolder);
        } else {
            myHolder = (MyHolder) convertView.getTag();
        }
        if (items != null) {
            myHolder.tv.setText(items[position]);
        }
        return convertView;
    }
}
