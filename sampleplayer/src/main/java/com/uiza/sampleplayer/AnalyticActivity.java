package com.uiza.sampleplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import android.os.Bundle;

import com.uiza.sdk.analytics.UZAnalytic;
import com.uiza.sdk.models.UZAnalyticInfo;
import com.uiza.sdk.models.UZEventType;
import com.uiza.sdk.models.UZTrackingData;
import com.uiza.sdk.utils.JacksonUtils;

import java.util.Arrays;
import java.util.UUID;

import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class AnalyticActivity extends AppCompatActivity {
    private AppCompatTextView txtLog;
    CompositeDisposable disposables;
    String sessionId;
    UZAnalyticInfo info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytic);
        txtLog = findViewById(R.id.txt_log);
        disposables = new CompositeDisposable();
        sessionId = UUID.randomUUID().toString();
        findViewById(R.id.one_event).setOnClickListener(v -> trackEvent());
        findViewById(R.id.some_events).setOnClickListener(v -> trackEvents());
        findViewById(R.id.live_viewers).setOnClickListener(v -> getLiveViewers());
        info = new UZAnalyticInfo("b963b465c34e4ffb9a71922442ee0dca", "b938c0a6-e9bc-4b25-9e66-dbf81d755c25", "live");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (disposables != null)
            disposables.dispose();
    }

    private void getLiveViewers() {
        disposables.add(UZAnalytic.getLiveViewers(info, liveCounter -> {
            Timber.e("onNext: %s", liveCounter.getViews());
            txtLog.setText(String.format("trackEvent::onNext: %s", JacksonUtils.toJson(liveCounter)));
        }, error -> {
            Timber.e("onError: %s", error.getMessage());
            txtLog.setText(String.format("getLiveViewers::onError: %s", error.getMessage()));
        }));
    }

    private void trackEvent() {
        UZTrackingData data = new UZTrackingData(info, sessionId);
        data.setEventType(UZEventType.WATCHING);
        disposables.add(UZAnalytic.pushEvent(data, responseBody -> {
                    Timber.e("onNext: %s", responseBody.contentLength());
                    txtLog.setText(String.format("trackEvent::onNext: %s", responseBody.contentLength()));
                },
                error -> {
                    Timber.e("onError: %s", error.getMessage());
                    txtLog.setText(String.format("trackEvent::onError: %s", error.getMessage()));
                }, () -> {
                    Timber.e("completed");
                }));
    }

    private void trackEvents() {
        UZTrackingData data1 = new UZTrackingData(info, sessionId);
        data1.setEventType(UZEventType.WATCHING);
        UZTrackingData data2 = new UZTrackingData(info, sessionId);
        data2.setEventType(UZEventType.WATCHING);
        disposables.add(UZAnalytic.pushEvents(Arrays.asList(data1, data2), responseBody -> {
                    Timber.e("onNext: %s", responseBody.contentLength());
                    txtLog.setText(String.format("trackEvents::onNext: %s", responseBody.contentLength()));
                },
                error -> {
                    Timber.e("onError: %s", error.getMessage());
                    txtLog.setText(String.format("trackEvents::onError: %s", error.getMessage()));
                }, () -> Timber.e("completed"))
        );
    }
}
