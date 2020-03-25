package com.uiza.sdk.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.UiModeManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;

import androidx.annotation.NonNull;

import com.uiza.sdk.R;
import com.uiza.sdk.widget.UZToast;

import timber.log.Timber;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
import static android.content.Context.ACTIVITY_SERVICE;

public class UZAppUtils {
    private UZAppUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }


    public static String getUserAgent(@NonNull Context context) {
        return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
    }

    public static boolean checkChromeCastAvailable() {
        return UZAppUtils.isDependencyAvailable("com.google.android.gms.cast.framework.OptionsProvider")
                && UZAppUtils.isDependencyAvailable("androidx.mediarouter.app.MediaRouteButton");
    }

    public static boolean isAdsDependencyAvailable() {
        return UZAppUtils.isDependencyAvailable("com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer");
    }

    public static boolean isDependencyAvailable(String dependencyClass) {
        try {
            Class.forName(dependencyClass);
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void vibrate(@NonNull Context context, int length) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null)
            v.vibrate(length);
    }

    public static void vibrate(@NonNull Context context) {
        vibrate(context, 300);
    }

    //return true if app is in foreground
    public static boolean isAppInForeground(@NonNull Context context) {
        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        if (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE) {
            return true;
        }
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        // App is foreground, but screen is locked, so show notification
        return km != null && km.inKeyguardRestrictedInputMode();
    }

    public static boolean checkServiceRunning(@NonNull Context context, String serviceName) {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceName.equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    //stop service pip FloatUizaVideoService
//    public static void stopMiniPlayer(@NonNull Context context) {
//        if (isMiniPlayerRunning(context))
//            context.stopService(new Intent(context, UZFloatVideoService.class)); //stop service if running
//    }
//
//    public static boolean isMiniPlayerRunning(@NonNull Context context) {
//        return checkServiceRunning(context, UZFloatVideoService.class.getName());
//    }

    public static boolean isTablet(@NonNull Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isTV(@NonNull Context context) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        return uiModeManager != null && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    public static void openUrlInBrowser(@NonNull Context context, String url) {
        Uri webPage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
        if (intent.resolveActivity(context.getPackageManager()) != null)
            context.startActivity(intent);
    }

    private static boolean checkAppInstall(@NonNull Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e);
        }
        return false;
    }

    public static void sharingToSocialMedia(@NonNull Context context, String application, String subject, String message) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType(Constants.TEXT_TYPE);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        boolean installed = checkAppInstall(context, application);
        if (installed) {
            intent.setPackage(application);
            context.startActivity(intent);
        } else {
            Timber.e(context.getString(R.string.can_not_find_share_app));
        }
    }

//    protected static void communicateContext(@NonNull Context context, String event) {
//        if (isMiniPlayerRunning(context))
//            CommunicateMng.postFromActivity(new CommunicateMng.MsgFromActivity(event));
//    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void moveTaskToFront(@NonNull Activity activity, boolean mIsRestoredToTop) {
        if (!activity.isTaskRoot() && mIsRestoredToTop) {
            // 4.4.2 platform issues for FLAG_ACTIVITY_REORDER_TO_FRONT,
            // reordered activity back press will go to home unexpectedly,
            // Workaround: move reordered activity current task to front when it's finished.
            ActivityManager tasksManager = (ActivityManager) activity.getSystemService(ACTIVITY_SERVICE);
            if (tasksManager != null)
                tasksManager.moveTaskToFront(activity.getTaskId(), ActivityManager.MOVE_TASK_NO_USER_ACTION);
        }
    }

    /**
     * Feature for {@link PackageManager#getSystemAvailableFeatures} and {@link PackageManager#hasSystemFeature}:
     * The device supports picture-in-picture multi-window mode.
     */
    public static boolean hasSupportPIP(@NonNull Context context) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE);
    }

    public static void setClipboard(@NonNull Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("COPY", text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            UZToast.show(context, "Copied!");
        }
    }
}
