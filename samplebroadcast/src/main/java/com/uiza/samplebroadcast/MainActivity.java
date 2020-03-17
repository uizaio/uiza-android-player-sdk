package com.uiza.samplebroadcast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    AppCompatEditText mServerEdt, mStreamKeyEdt;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mServerEdt = findViewById(R.id.edt_server);
        mStreamKeyEdt = findViewById(R.id.edt_stream_key);
        mServerEdt.setText("rtmp://asia-southeast1-u-01-gw.uiza.io:1935/push2transcode");
        mStreamKeyEdt.setText("2978a34b-5bdb-4feb-bd95-75e086328726?token=dbe532ac6a62d7d1ae260cc8eeea5446&ulasId=d16196ce-e451-403f-ba4b-866fbe290398");
        findViewById(R.id.btn_start).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UZBroadCastActivity.class);
            intent.putExtra(SampleLiveApplication.EXTRA_STREAM_ENDPOINT, String.format("%s/%s", mServerEdt.getText().toString(), mStreamKeyEdt.getText().toString()));
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                launchActivity(SettingsActivity.class);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private <T extends Activity> void launchActivity(Class<T> tClass) {
        startActivity(new Intent(MainActivity.this, tClass));
    }
}
