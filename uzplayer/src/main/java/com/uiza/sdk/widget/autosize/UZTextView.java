package com.uiza.sdk.widget.autosize;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.appcompat.widget.AppCompatTextView;

import com.uiza.sdk.R;

/**
 * Created by loitp on 4/19/2018.
 */

public class UZTextView extends AppCompatTextView {
    private boolean isUseDefault;
    private boolean isLandscape;
    private int textSizeLand = -1;
    private int textSizePortrait = -1;

    public UZTextView(Context context) {
        super(context);
        init(null, 0);
    }

    public UZTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public UZTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.UZTextView, defStyleAttr, 0);
            try {
                isUseDefault = a.getBoolean(R.styleable.UZTextView_useDefaultTV, true);
            } finally {
                a.recycle();
            }
        } else {
            isUseDefault = true;
        }
        setShadowLayer(
                1f, // radius
                1f, // dx
                1f, // dy
                Color.BLACK // shadow color
        );
        updateSize();
        setSingleLine();
    }

    private void updateSize() {
        if (!isUseDefault) return;
        setTextSize(TypedValue.COMPLEX_UNIT_SP, isLandscape ? getTextSizeLand() : getTextSizePortrait());
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        updateSize();
    }

    public int getTextSizeLand() {
        return textSizeLand == -1 ? 15 : textSizeLand;
    }

    //sp
    public void setTextSizeLand(int textSizeLand) {
        this.textSizeLand = textSizeLand;
    }

    public int getTextSizePortrait() {
        return textSizePortrait == -1 ? 10 : textSizePortrait;
    }

    //sp
    public void setTextSizePortrait(int textSizePortrait) {
        this.textSizePortrait = textSizePortrait;
    }
}
