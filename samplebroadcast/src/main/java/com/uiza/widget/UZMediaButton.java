package com.uiza.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.Checkable;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageButton;

import com.uiza.samplebroadcast.R;

public class UZMediaButton extends AppCompatImageButton implements Checkable {

    private int activeDrawableId = -1, inActiveDrawableId = -1;
    private boolean checked = false;


    public UZMediaButton(Context context) {
        this(context, null);
    }

    public UZMediaButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs, 0);
    }

    public UZMediaButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs, defStyleAttr);
    }

    private void initView(AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.UZMediaButton, defStyleAttr, 0);
            activeDrawableId = a.getResourceId(R.styleable.UZMediaButton_srcActive, -1);
            inActiveDrawableId = a.getResourceId(R.styleable.UZMediaButton_srcInactive, -1);
        } else {
            activeDrawableId = -1;
            inActiveDrawableId = -1;
        }
        updateDrawable();
    }

    private void updateDrawable() {
        setImageDrawable(AppCompatResources.getDrawable(getContext(), checked ? activeDrawableId : inActiveDrawableId));
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void setChecked(boolean checked) {
        if (this.checked != checked) {
            this.checked = checked;
            updateDrawable();
            refreshDrawableState();
        }
    }

    @Override
    public void toggle() {
        checked = !checked;
    }
}
