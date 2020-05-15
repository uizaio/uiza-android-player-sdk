package com.uiza.sampleplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_player).setOnClickListener(v -> gotoActivity(PlayerActivity.class));
        findViewById(R.id.btn_pip_player).setOnClickListener(v -> gotoActivity(PipPlayerActivity.class));
        findViewById(R.id.btn_cast_player).setVisibility(View.GONE);
        findViewById(R.id.btn_analytic).setOnClickListener(v -> gotoActivity(AnalyticActivity.class));
        //.setOnClickListener(v -> gotoActivity(CastPlayerActivity.class));
        ((AppCompatTextView) findViewById(R.id.txt_version)).setText(String.format(Locale.getDefault(),
                "%s (%s)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
    }

    private <T> void gotoActivity(Class<T> clazz) {
        startActivity(new Intent(MainActivity.this, clazz));
    }
}