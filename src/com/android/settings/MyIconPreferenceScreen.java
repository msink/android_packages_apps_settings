/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MyIconPreferenceScreen extends Preference {

    private Drawable mIcon;
    private Drawable mBg;
    private int mWidth;
    private int mHeight;

    public MyIconPreferenceScreen(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.mypreference_icon);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.MyIconPreferenceScreen, defStyle, 0);
        mIcon = a.getDrawable(R.styleable.MyIconPreferenceScreen_settingicon);
        mBg = a.getDrawable(R.styleable.MyIconPreferenceScreen_bg);
        mWidth = (int) a.getDimension(R.styleable.MyIconPreferenceScreen_width, 758);
        mHeight = (int) a.getDimension(R.styleable.MyIconPreferenceScreen_height, 70);
    }

    public MyIconPreferenceScreen(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyIconPreferenceScreen(Context context) {
        this(context, null, 0);
    }

    public void setmBg(Drawable bg) {
        mBg = bg;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        view.setBackgroundColor(0);
        ImageView imageView = (ImageView) view.findViewById(R.id.settingsicon);
        if (imageView != null && mIcon != null) {
            imageView.setImageDrawable(mIcon);
        }
        Paint mp = new Paint();
        mp.setTypeface(Typeface.DEFAULT_BOLD);
        RelativeLayout relatePreference = (RelativeLayout)
            view.findViewById(R.id.relate_preference);
        if (relatePreference != null && mBg != null) {
            relatePreference.setBackgroundDrawable(mBg);
        }
        if (relatePreference != null) {
            ViewGroup.LayoutParams linearParams = relatePreference.getLayoutParams();
            linearParams.width = mWidth;
            linearParams.height = mHeight;
            relatePreference.setLayoutParams(linearParams);
        }
    }
}
