package com.uiza.sdk.analytics;

import com.uiza.sdk.BuildConfig;
import com.uiza.sdk.models.UZTrackingBody;
import com.uiza.sdk.models.UZTrackingData;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import okhttp3.ResponseBody;

public final class UZAnalytic {

    private static String sdkVersionName; //UZData/AndroidSDK/1.1.0
    private static String deviceId;
    private static boolean prodEnv = false;

    private UZAnalytic() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * @param deviceId: DeviceId or AndroidId
     */

    public static void init(String deviceId, boolean prodEnv) {
        UZAnalytic.deviceId = deviceId;
        UZAnalytic.sdkVersionName = String.format("UZData/AndroidSDK/%s", BuildConfig.VERSION_NAME);
        UZAnalytic.prodEnv = prodEnv;
    }

    public static String getDeviceId() {
        return deviceId;
    }

    public static boolean isProdEnv() {
        return prodEnv;
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

}
