package com.uiza.sdk.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.uiza.sdk.view.UZPlayerView;

public class UZVideoViewOverlay extends ViewGroup implements UZPlayerView.OnDoubleTap {

    public UZVideoViewOverlay(Context context) {
        super(context);
    }

    public UZVideoViewOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UZVideoViewOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public UZVideoViewOverlay(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}
