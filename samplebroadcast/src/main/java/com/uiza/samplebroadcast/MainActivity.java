package com.uiza.samplebroadcast;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    AppCompatEditText mServerEdt, mStreamKeyEdt;
    AppCompatSpinner profileSelector;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mServerEdt = findViewById(R.id.edt_server);
        mStreamKeyEdt = findViewById(R.id.edt_stream_key);
        profileSelector = findViewById(R.id.sp_profile);
        String[] profiles = getResources().getStringArray(R.array.profile_values);
        mServerEdt.setText("rtmp://vn-southeast-1-u-04-gw.uiza.io:1935/push2transcode");
        mStreamKeyEdt.setText("6fb62b90-7615-46d3-8695-3baf7eee242d?token=51e574e1d4eb91a8839906cf8cd12e55&ulasId=3cfb20e8-015d-4b4d-95dd-f237d28d6e60");
        findViewById(R.id.btn_start).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UZBroadCastActivity.class);
            intent.putExtra(SampleLiveApplication.EXTRA_STREAM_ENDPOINT, String.format("%s/%s", mServerEdt.getText().toString(), mStreamKeyEdt.getText().toString()));
            intent.putExtra(SampleLiveApplication.EXTRA_STREAM_PROFILE, Integer.valueOf(profiles[profileSelector.getSelectedItemPosition()]));
            startActivity(intent);
        });
    }
}
