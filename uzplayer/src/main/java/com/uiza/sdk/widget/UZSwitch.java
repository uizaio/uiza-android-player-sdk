package com.uiza.sdk.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.Switch;

import androidx.core.content.res.ResourcesCompat;

import com.uiza.sdk.R;

public class UZSwitch extends Switch {

    public UZSwitch(Context context) {
        super(context);
    }

    public UZSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UZSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        changeColor(checked);
    }

    private void changeColor(boolean isChecked) {
        int thumbColor;
        int trackColor;
        if (isChecked) {
            thumbColor = ResourcesCompat.getColor(getResources(), R.color.red, getContext().getTheme());
            trackColor = Color.argb(200, 230, 57, 70);
        } else {
            thumbColor = ResourcesCompat.getColor(getResources(), R.color.light_grey, getContext().getTheme());
            trackColor = Color.argb(160, 211, 211, 211);
        }
        try {
            getThumbDrawable().setColorFilter(thumbColor, PorterDuff.Mode.MULTIPLY);
            getTrackDrawable().setColorFilter(trackColor, PorterDuff.Mode.MULTIPLY);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}