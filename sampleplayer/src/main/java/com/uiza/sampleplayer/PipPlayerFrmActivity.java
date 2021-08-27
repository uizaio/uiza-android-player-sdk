package com.uiza.sampleplayer;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class PipPlayerFrmActivity extends AppCompatActivity {

    private final FragmentPlayer fragmentPlayer = new FragmentPlayer();

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_pip_player_frm);

        switchFragment(fragmentPlayer);
    }

    private void switchFragment(Fragment fragment) {
        try {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.layout_container, fragment);
            ft.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        fragmentPlayer.onUserLeaveHint();
    }
}
