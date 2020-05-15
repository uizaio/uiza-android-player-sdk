package com.uiza.sampleplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import android.os.Bundle;

import com.uiza.sdk.analytics.UZAnalytic;
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
    private static final String ENTITY_ID = "6682f3fe-72f7-46a2-a2fb-a9bbc74e8356";
    String sessionId;
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (disposables != null)
            disposables.dispose();
    }

    private void getLiveViewers() {
        disposables.add(UZAnalytic.getLiveViewers(ENTITY_ID, liveCounter -> {
            Timber.e("onNext: %s", liveCounter.getViews());
            txtLog.setText(String.format("trackEvent::onNext: %s", JacksonUtils.toJson(liveCounter)));
        }, error -> {
            Timber.e("onError: %s", error.getMessage());
            txtLog.setText(String.format("getLiveViewers::onError: %s", error.getMessage()));
        }));
    }

    private void trackEvent() {
        UZTrackingData data = new UZTrackingData(sessionId);
        data.setEventType(UZEventType.WATCHING);
        data.setEntityId(ENTITY_ID);
        data.setEntitySource("live");
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
        UZTrackingData data1 = new UZTrackingData(sessionId);
        data1.setEventType(UZEventType.WATCHING);
        data1.setEntityId(ENTITY_ID);
        data1.setEntitySource("live");
        UZTrackingData data2 = new UZTrackingData(sessionId);
        data2.setEventType(UZEventType.WATCHING);
        data2.setEntityId(ENTITY_ID);
        data2.setEntitySource("live");
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
