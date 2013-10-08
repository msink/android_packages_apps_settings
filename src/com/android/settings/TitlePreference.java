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
    private Drawable mBg;
    private Drawable mIcon;

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
        RelativeLayout relatePreference = (RelativeLayout)
                view.findViewById(R.id.relate_preference_title);
        if (relatePreference != null && mBg != null) {
            relatePreference.setBackgroundDrawable(mBg);
        }
    }
}
