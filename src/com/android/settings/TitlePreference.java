package com.android.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.util.AttributeSet;

public class TitlePreference extends Preference {
    private Drawable mIcon;
    private ImageView imgBatteryView;
    private int mDrawableResId = 0;
    private RelativeLayout relatePreference;

    public TitlePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.preference_title);
        TypedArray a = context.obtainStyledAttributes(attrs,
                 R.styleable.MyIconPreferenceScreen, defStyle, 0);
        mIcon = a.getDrawable(0);
    }

    public TitlePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void onBindView(View view) {
        super.onBindView(view);
        ImageView imageView = (ImageView)
                view.findViewById(R.id.title_icon);
        if (imageView != null && mIcon != null) {
            imageView.setBackgroundDrawable(mIcon);
        }
        relatePreference = (RelativeLayout)view.findViewById(R.id.relate_preference_title);
        imgBatteryView = (ImageView)view.findViewById(R.id.image_battery);
        if (relatePreference != null && mDrawableResId != 0) {
            imgBatteryView.setBackgroundResource(mDrawableResId);
        }
    }

    public void setmBgResuID(int mResID) {
        mDrawableResId = mResID;
        if (imgBatteryView != null && mDrawableResId != 0) {
            imgBatteryView.setBackgroundResource(mDrawableResId);
        }
    }
}
