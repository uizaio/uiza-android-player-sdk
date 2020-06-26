package com.uiza.sdk.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.google.android.exoplayer2.ui.PlayerView;
import com.uiza.sdk.R;
import com.uiza.sdk.widget.UZImageButton;

import java.util.ArrayList;
import java.util.Locale;

import timber.log.Timber;

public class UZViewUtils {
    public static ArrayList<View> getAllChildren(@NonNull View v) {
        ArrayList<View> result = new ArrayList<>();
        if (!(v instanceof ViewGroup)) {
            result.add(v);
            return result;
        }
        ViewGroup viewGroup = (ViewGroup) v;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            ArrayList<View> viewArrayList = new ArrayList<>();
            viewArrayList.add(v);
            viewArrayList.addAll(getAllChildren(child));
            result.addAll(viewArrayList);
        }
        return result;
    }

    public static boolean isFullScreen(@NonNull Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            final int rotation = windowManager.getDefaultDisplay().getRotation();
            switch (rotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    return false;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                default:
                    return true;
            }
        }
        return false;
    }

    //return true if device is set auto switch rotation on
    //return false if device is set auto switch rotation off
    public static boolean isRotationPossible(@NonNull Context context) {
        boolean hasAccelerometer = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
        return (hasAccelerometer && android.provider.Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
    }

    public static int getScreenHeightIncludeNavigationBar(@NonNull Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            final Display display = windowManager.getDefaultDisplay();
            Point outPoint = new Point();
            // include navigation bar
            display.getRealSize(outPoint);
            return Math.max(outPoint.y, outPoint.x);
        }
        return 0;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static void visibleViews(View... views) {
        for (View v : views) {
            if (v != null && v.getVisibility() != View.VISIBLE)
                v.setVisibility(View.VISIBLE);
        }
    }

    public static void goneViews(View... views) {
        for (View v : views) {
            if (v != null && v.getVisibility() != View.GONE)
                v.setVisibility(View.GONE);
        }
    }

    public static void invisibleViews(View... views) {
        for (View v : views) {
            if (v != null && v.getVisibility() != View.INVISIBLE)
                v.setVisibility(View.INVISIBLE);
        }
    }

    public static void setVisibilityViews(int visibility, View... views) {
        for (View v : views) {
            if (v != null && v.getVisibility() != visibility)
                v.setVisibility(visibility);
        }
    }

    //return pixel
    public static int heightOfView(@NonNull View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        return view.getMeasuredHeight();
    }

    public static boolean isCanOverlay(@NonNull Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public static void changeScreenPortrait(@NonNull Activity activity) {
        if (getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE)
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public static void changeScreenLandscape(@NonNull Activity activity) {
        if (getScreenOrientation() == Configuration.ORIENTATION_PORTRAIT)
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public static void changeScreenLandscape(@NonNull Activity activity, int orientation) {
        if (getScreenOrientation() == Configuration.ORIENTATION_PORTRAIT) {
            if (orientation == 90) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            } else if (orientation == 270) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }

    public static int getScreenOrientation() {
        return Resources.getSystem().getConfiguration().orientation;
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public static void toggleScreenOrientation(@NonNull Activity activity) {
        int s = getScreenOrientation();
        if (s == Configuration.ORIENTATION_LANDSCAPE)
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        else if (s == Configuration.ORIENTATION_PORTRAIT) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }

    public static void hideSystemUiFullScreen(@NonNull PlayerView playerView) {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
    }

    public static void hideSystemUi(@NonNull PlayerView playerView) {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                & ~View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                & ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                & ~View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                & ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
    }

    public static void setColorProgressBar(@NonNull ProgressBar progressBar, @ColorInt int color) {
        progressBar.getIndeterminateDrawable().setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
    }

    public static void setTextShadow(@NonNull TextView textView, @ColorInt int color) {
        textView.setShadowLayer(
                1f, // radius
                1f, // dx
                1f, // dy
                color // shadow color
        );
    }

    public static void setMarginPx(@NonNull View view, int l, int t, int r, int b) {
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        mlp.setMargins(l, t, r, b);
        view.requestLayout();
    }

    public static void setMarginDimen(@NonNull View view, int dpL, int dpT, int dpR, int dpB) {
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        mlp.setMargins(ConvertUtils.dp2px(dpL), ConvertUtils.dp2px(dpT), ConvertUtils.dp2px(dpR), ConvertUtils.dp2px(dpB));
        view.requestLayout();
    }

    public static void setFocusableViews(boolean focusable, View... views) {
        for (View v : views) {
            if (v != null && !v.isFocusable())
                v.setFocusable(focusable);
        }
    }


    public static void setSrcDrawableEnabledForViews(UZImageButton... views) {
        for (UZImageButton v : views) {
            if (v != null && !v.isFocused()) {
                v.setSrcDrawableEnabled();
            }
        }
    }

    public static void setClickableForViews(boolean able, View... views) {
        for (View v : views) {
            if (v != null) {
                v.setClickable(able);
                v.setFocusable(able);
            }
        }
    }


    public static void setUIFullScreenIcon(@NonNull ImageButton imageButton, boolean isFullScreen) {
        imageButton.setImageResource(isFullScreen ?
                R.drawable.ic_fullscreen_exit_white_48
                : R.drawable.ic_fullscreen_white_48);
    }

    public static void resizeLayout(@NonNull ViewGroup viewGroup, ImageView ivVideoCover, int pixelAdded, int videoW, int videoH, boolean isFreeSize) {
        int widthSurfaceView = 0;
        int heightSurfaceView = 0;
        boolean isFullScreen = UZViewUtils.isFullScreen(viewGroup.getContext());
        if (isFullScreen) {//landscape
            widthSurfaceView = UZViewUtils.getScreenHeightIncludeNavigationBar(viewGroup.getContext());
            heightSurfaceView = UZViewUtils.getScreenHeight();
        } else {//portrait
            widthSurfaceView = UZViewUtils.getScreenWidth();
            if (videoW == 0 || videoH == 0) {
                heightSurfaceView = (int) (widthSurfaceView * Constants.RATIO_9_16) + pixelAdded;
            } else {
                if (videoW >= videoH) {
                    heightSurfaceView = isFreeSize ? (widthSurfaceView * videoH / videoW + pixelAdded)
                            : ((int) (widthSurfaceView * Constants.RATIO_9_16) + pixelAdded);
                } else {
                    heightSurfaceView = isFreeSize ? (widthSurfaceView * videoH / videoW + pixelAdded)
                            : ((int) (widthSurfaceView * Constants.RATIO_9_16) + pixelAdded);
                }
            }
        }
        //LLog.d(TAG, "resizeLayout isFullScreen " + isFullScreen + ", widthSurfaceView x heightSurfaceView: " + widthSurfaceView + "x" + heightSurfaceView + ", pixelAdded: " + pixelAdded + ", videoW: " + videoW + ", videoH: " + videoH);
        viewGroup.getLayoutParams().width = widthSurfaceView;
        viewGroup.getLayoutParams().height = heightSurfaceView;
        viewGroup.requestLayout();
        //set size of parent view group of viewGroup
        RelativeLayout parentViewGroup = (RelativeLayout) viewGroup.getParent();
        if (parentViewGroup != null) {
            parentViewGroup.getLayoutParams().width = widthSurfaceView;
            parentViewGroup.getLayoutParams().height = heightSurfaceView;
            parentViewGroup.requestLayout();
        }
        if (ivVideoCover != null) {
            ivVideoCover.getLayoutParams().width = widthSurfaceView;
            ivVideoCover.getLayoutParams().height = heightSurfaceView - pixelAdded;
            ivVideoCover.requestLayout();
        }
        //edit size of imageview thumnail
        FrameLayout flImgThumnailPreviewSeekbar = viewGroup.findViewById(R.id.preview_frame_layout);
        if (flImgThumnailPreviewSeekbar != null) {
            if (isFullScreen) {
                flImgThumnailPreviewSeekbar.getLayoutParams().width = widthSurfaceView / 4;
                flImgThumnailPreviewSeekbar.getLayoutParams().height = (int) (widthSurfaceView / 4 * Constants.RATIO_9_16);
            } else {
                flImgThumnailPreviewSeekbar.getLayoutParams().width = widthSurfaceView / 5;
                flImgThumnailPreviewSeekbar.getLayoutParams().height = (int) (widthSurfaceView / 5 * Constants.RATIO_9_16);
            }
            flImgThumnailPreviewSeekbar.requestLayout();
        }
    }

    public static void showDialog(@NonNull Dialog dialog) {
        boolean isFullScreen = isFullScreen(dialog.getContext());
        Window window = dialog.getWindow();
        if (window == null) return;
        if (isFullScreen) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            } else
                Timber.w("cần làm ở sdk thấp, thanh navigation ko chịu ẩn");
        }
        dialog.show();
        try {
            window.getAttributes().windowAnimations = R.style.uiza_dialog_animation;
            window.setBackgroundDrawableResource(R.drawable.background_dialog_uiza);
            //set dialog position
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.BOTTOM;
            //wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            wlp.dimAmount = 0.65f;
            wlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            wlp.height = ViewGroup.LayoutParams.WRAP_CONTENT;  // (int) (getScreenHeight() * (isFullScreen ? 0.6 : 0.4));
            window.setAttributes(wlp);
        } catch (Exception e) {
            //do nothing
            Timber.e(e);
        }
        if (isFullScreen)
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    public static void setTextDuration(@NonNull TextView textView, String duration) {
        if (TextUtils.isEmpty(duration)) return;
        try {
            int min = (int) Double.parseDouble(duration) + 1;
            String minutes = Integer.toString(min % 60);
            minutes = minutes.length() == 1 ? "0" + minutes : minutes;
            textView.setText(String.format(Locale.getDefault(), "%d:%s", (min / 60), minutes));
        } catch (Exception e) {
            Timber.e(e, "Error setTextDuration");
            textView.setText(" - ");
        }
    }

    public static void updateUIFocusChange(@NonNull View view, boolean isFocus) {
        updateUIFocusChange(view, isFocus, R.drawable.bkg_has_focus, R.drawable.bkg_no_focus);
    }

    public static void updateUIFocusChange(@NonNull View view, boolean isFocus, int resHasFocus, int resNoFocus) {
        view.setBackgroundResource(isFocus ? resHasFocus : resNoFocus);
    }
}
