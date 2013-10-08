package com.android.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.preference.Preference;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.util.AttributeSet;

public class ChildTitlePreference extends Preference {
    private ImageView imageView;
    private Drawable mBg;
    private int mHeight;
    private int mWidth;

    public ChildTitlePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.preference_child_title);
        TypedArray a = context.obtainStyledAttributes(attrs,
                 R.styleable.MyIconPreferenceScreen, defStyle, 0);
        mWidth = (int) a.getDimension(2, 758);
        mHeight = (int) a.getDimension(3, 55);
    }

    public ChildTitlePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private View.OnTouchListener imageViewOnTouchListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
            case 0:
                imageView.setBackgroundResource(R.drawable.settings_back_fcs);
                break;
            case 1:
                imageView.setBackgroundResource(R.drawable.settings_back);
                break;
            }
            return false;
        }
    };

    public void onBindView(View view) {
        super.onBindView(view);
        view.setBackgroundColor(0);
        RelativeLayout relatePreference = (RelativeLayout)
                view.findViewById(R.id.relate_preference_title);
        if (relatePreference != null && mBg != null) {
            relatePreference.setBackgroundDrawable(mBg);
        }
        if (relatePreference != null) {
            ViewGroup.LayoutParams linearParams;
            linearParams = relatePreference.getLayoutParams();
            linearParams.width = mWidth;
            linearParams.height = mHeight;
            relatePreference.setLayoutParams(linearParams);
        }
        Paint mp = new Paint();
        mp.setTypeface(Typeface.DEFAULT_BOLD);
        imageView = (ImageView) view.findViewById(R.id.icon);
        if (imageView != null) {
            imageView.setOnTouchListener(imageViewOnTouchListener);
        }
    }
}
