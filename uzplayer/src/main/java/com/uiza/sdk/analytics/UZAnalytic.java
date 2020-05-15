package com.uiza.sdk.analytics;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.uiza.sdk.BuildConfig;
import com.uiza.sdk.models.UZAnalyticInfo;
import com.uiza.sdk.models.UZTrackingBody;
import com.uiza.sdk.models.UZTrackingData;
import com.uiza.sdk.models.UZLiveCounter;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import okhttp3.ResponseBody;

public final class UZAnalytic {
    private static String sdkVersionName; //UZData/AndroidSDK/1.1.0
    private static String deviceId;

    private UZAnalytic() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    private static void init(String deviceId) {
        UZAnalytic.sdkVersionName = String.format("UZData/AndroidPlayerSDK/%s", BuildConfig.VERSION_NAME);
        UZAnalytic.deviceId = deviceId;
    }

    @SuppressLint("HardwareIds")
    public static void init(@NonNull Context context, String baseAnalyticUrl, String baseLiveCountUrl) {
        init(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
        UZAnalyticClient.getInstance().init(baseAnalyticUrl);
        UZLiveCountClient.getInstance().init(baseLiveCountUrl);
    }

    public static String getDeviceId() {
        return deviceId;
    }

    public static String getSourceName() {
        return sdkVersionName;
    }


    public static Disposable pushEvent(UZTrackingData data, Consumer<ResponseBody> onNext, Consumer<Throwable> onError) {
        return RxBinder.bind(UZAnalyticClient.getInstance().createAnalyticAPI().pushEvents(UZTrackingBody.create(data)), onNext, onError);
    }

    public static Disposable pushEvent(UZTrackingData data, Consumer<ResponseBody> onNext, Consumer<Throwable> onError, Action onComplete) {
        return RxBinder.bind(UZAnalyticClient.getInstance().createAnalyticAPI().pushEvents(UZTrackingBody.create(data)), onNext, onError, onComplete);
    }

    public static Disposable pushEvents(List<UZTrackingData> data, Consumer<ResponseBody> onNext, Consumer<Throwable> onError) {
        return RxBinder.bind(UZAnalyticClient.getInstance().createAnalyticAPI().pushEvents(UZTrackingBody.create(data)), onNext, onError);
    }

    public static Disposable pushEvents(List<UZTrackingData> data, Consumer<ResponseBody> onNext, Consumer<Throwable> onError, Action onComplete) {
        return RxBinder.bind(UZAnalyticClient.getInstance().createAnalyticAPI().pushEvents(UZTrackingBody.create(data)), onNext, onError, onComplete);
    }

    public static Disposable getLiveViewers(UZAnalyticInfo info, Consumer<UZLiveCounter> onNext, Consumer<Throwable> onError) {
        return RxBinder.bind(UZLiveCountClient.getInstance().createLiveViewCountAPI().getLiveViewers(info.getAppId(), info.getEntityId()), onNext, onError);
    }
}
