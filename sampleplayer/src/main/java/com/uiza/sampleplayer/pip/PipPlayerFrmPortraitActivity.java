package com.uiza.sampleplayer.pip;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.uiza.sampleplayer.R;

public class PipPlayerFrmPortraitActivity extends AppCompatActivity {

    private final FragmentPlayerPortrait fragmentPlayerPortrait = new FragmentPlayerPortrait();

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_pip_player_frm);

        switchFragment(fragmentPlayerPortrait);
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
        fragmentPlayerPortrait.onUserLeaveHint();
    }
}
