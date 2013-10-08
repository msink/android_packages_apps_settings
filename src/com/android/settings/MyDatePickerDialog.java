package com.android.settings;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.DatePicker;
import android.widget.Toast;

public class MyDatePickerDialog extends DatePickerDialog {
    private Context mContext;

    public MyDatePickerDialog(Context context,
                              DatePickerDialog.OnDateSetListener callBack,
                              int year, int monthOfYear, int dayOfMonth) {
        super(context, callBack, year, monthOfYear, dayOfMonth);
        mContext = context;
    }

    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
    }

    public void onDateChanged(DatePicker view, int year, int month, int day) {
        if (year <= 1970) {
            Toast.makeText(mContext, "Please select 1970 or later ", 0).show();
        } else if (year >= 2038) {
            Toast.makeText(mContext, "Please select 2038 before ", 0).show();
        } else {
            super.onDateChanged(view, year, month, day);
        }
    }

    public void updateDate(int year, int monthOfYear, int dayOfMonth) {
        if (year <= 1970) {
            year = 1970;
        }
        super.updateDate(year, monthOfYear, dayOfMonth);
    }
}
