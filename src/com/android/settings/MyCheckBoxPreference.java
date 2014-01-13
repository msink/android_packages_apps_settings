package com.android.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.preference.CheckBoxPreference;
import android.view.View;
import android.widget.LinearLayout;
import android.util.AttributeSet;

public class MyCheckBoxPreference extends CheckBoxPreference {
    private Drawable mBg;

    public MyCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setLayoutResource(R.layout.mycheckboxpreference);
        mBg = context.obtainStyledAttributes(attrs,
                 R.styleable.myCheckBoxPreference, defStyle, 0).getDrawable(0);
    }

    public MyCheckBoxPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyCheckBoxPreference(Context context) {
        this(context, null);
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        LinearLayout myCheckBoxLayout = (LinearLayout) view.findViewById(R.id.mylinearcheck);
        if (myCheckBoxLayout != null && mBg != null) {
            myCheckBoxLayout.setBackgroundDrawable(null);
        }
    }

    public void setIcon(Drawable icon) {
        if ((icon == null && mBg != null) || (icon != null && !icon.equals(mBg))) {
            mBg = icon;
            notifyChanged();
        }
    }
}
