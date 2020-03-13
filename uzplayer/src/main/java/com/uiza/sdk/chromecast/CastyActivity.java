package com.uiza.sdk.chromecast;

import android.os.Bundle;
import android.view.Menu;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.uiza.sdk.R;

/**
 * Extensible {@link AppCompatActivity}, which helps with setting widgets
 */
public abstract class CastyActivity extends AppCompatActivity {
    protected Casty casty;

    @CallSuper
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        casty = Casty.create(this);
    }

    @CallSuper
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (findViewById(R.id.casty_mini_controller) == null) {
            casty.addMiniController();
        }
        casty.addMediaRouteMenuItem(menu);
        return true;
    }
}
