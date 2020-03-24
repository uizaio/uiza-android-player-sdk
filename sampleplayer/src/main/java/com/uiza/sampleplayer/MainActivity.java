package com.uiza.sampleplayer;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_player).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PlayerActivity.class)));
        findViewById(R.id.btn_pip_player).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PipPlayerActivity.class)));
    }
}
