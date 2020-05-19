package com.uiza.sdk.analytics;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.util.UriUtil;
import com.uiza.sdk.BuildConfig;
import com.uiza.sdk.models.UZAnalyticInfo;
import com.uiza.sdk.models.UZTrackingBody;
import com.uiza.sdk.models.UZTrackingData;
import com.uiza.sdk.models.UZLiveCounter;
import com.uiza.sdk.utils.StringUtils;
import com.uiza.sdk.utils.UZAppUtils;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import okhttp3.ResponseBody;
import timber.log.Timber;

public final class UZAnalytic {
    private static String sdkVersionName; //UZData/AndroidSDK/1.1.0
    private static String deviceId;

    private UZAnalytic() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * @param context: ApplicationContext
     */
    @SuppressLint("HardwareIds")
    public static void init(@NonNull Context context) {
        UZAnalytic.deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        UZAnalytic.sdkVersionName = String.format("UZData/AndroidPlayerSDK/%s", BuildConfig.VERSION_NAME);
        String analyticUrl = UZAppUtils.getMetaData(context, "uz_analytic_url");
        if (URLUtil.isValidUrl(analyticUrl))
            UZAnalyticClient.getInstance().init(analyticUrl);
        else
            Timber.w("Please check settings 'uz_analytic_url' in AndroidManifest.xml");
        String liveViewsUrl = UZAppUtils.getMetaData(context, "uz_live_views_url");
        if (URLUtil.isValidUrl(liveViewsUrl))
            UZLiveViewsClient.getInstance().init(liveViewsUrl);
        else
            Timber.w("Please check settings 'uz_live_views_url' in AndroidManifest.xml");
    }

    public static String getDeviceId() {
        return deviceId;
    }

    public static String getSourceName() {
        return sdkVersionName;
    }


    public static Disposable pushEvent(UZTrackingData data, Consumer<ResponseBody> onNext,
                                       Consumer<Throwable> onError) throws IllegalStateException {
        return RxBinder.bind(UZAnalyticClient.getInstance().createAnalyticAPI().pushEvents(UZTrackingBody.create(data)), onNext, onError);
    }

    public static Disposable pushEvent(UZTrackingData data, Consumer<ResponseBody> onNext,
                                       Consumer<Throwable> onError, Action onComplete) throws IllegalStateException {
        return RxBinder.bind(UZAnalyticClient.getInstance().createAnalyticAPI().pushEvents(UZTrackingBody.create(data)), onNext, onError, onComplete);
    }

    public static Disposable pushEvents(List<UZTrackingData> data, Consumer<ResponseBody> onNext,
                                        Consumer<Throwable> onError) throws IllegalStateException {
        return RxBinder.bind(UZAnalyticClient.getInstance().createAnalyticAPI().pushEvents(UZTrackingBody.create(data)), onNext, onError);
    }

    public static Disposable pushEvents(List<UZTrackingData> data, Consumer<ResponseBody> onNext,
                                        Consumer<Throwable> onError, Action onComplete) throws IllegalStateException {
        return RxBinder.bind(UZAnalyticClient.getInstance().createAnalyticAPI().pushEvents(UZTrackingBody.create(data)), onNext, onError, onComplete);
    }

    public static Disposable getLiveViewers(UZAnalyticInfo info, Consumer<UZLiveCounter> onNext,
                                            Consumer<Throwable> onError) throws IllegalStateException {
        return RxBinder.bind(UZLiveViewsClient.getInstance().createLiveViewCountAPI().getLiveViewers(info.getAppId(), info.getEntityId()), onNext, onError);
    }
}
