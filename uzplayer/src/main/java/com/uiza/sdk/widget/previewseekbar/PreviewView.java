package com.uiza.sdk.widget.previewseekbar;

import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.widget.FrameLayout;


public interface PreviewView {

    int getProgress();

    int getMax();

    int getThumbOffset();

    int getDefaultColor();

    boolean isShowingPreview();

    void showPreview();

    void hidePreview();

    void setPreviewLoader(PreviewLoader previewLoader);

    void setPreviewColorTint(@ColorInt int color);

    void setPreviewColorResourceTint(@ColorRes int color);

    void attachPreviewFrameLayout(@Nullable FrameLayout frameLayout);

    void addOnPreviewChangeListener(OnPreviewChangeListener listener);

    void removeOnPreviewChangeListener(@Nullable OnPreviewChangeListener listener);

    interface OnPreviewChangeListener {
        void onStartPreview(@Nullable PreviewView previewView, int progress);

        void onStopPreview(@Nullable PreviewView previewView, int progress);

        void onPreview(@Nullable PreviewView previewView, int progress, boolean fromUser);
    }
}
