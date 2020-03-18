package com.uiza.sdk.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.MediaRouteButton;
import android.util.AttributeSet;


import com.uiza.sdk.exceptions.ErrorConstant;
import com.uiza.sdk.util.UZAppUtils;

public class UZMediaRouteButton extends MediaRouteButton {

    protected Drawable mRemoteIndicatorDrawable;

    {
        checkChromeCastAvailable();
    }

    public UZMediaRouteButton(Context context) {
        super(context);
    }

    public UZMediaRouteButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UZMediaRouteButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setRemoteIndicatorDrawable(Drawable d) {
        mRemoteIndicatorDrawable = d;
        super.setRemoteIndicatorDrawable(d);
    }

    public void applyTint(int color) {
        Drawable wrapDrawable = DrawableCompat.wrap(mRemoteIndicatorDrawable);
        DrawableCompat.setTint(wrapDrawable, color);
    }

    private void checkChromeCastAvailable() {
        if (!UZAppUtils.checkChromeCastAvailable()) {
            throw new NoClassDefFoundError(ErrorConstant.ERR_505);
        }
    }
}