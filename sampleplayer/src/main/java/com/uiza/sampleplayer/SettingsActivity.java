package com.uiza.sampleplayer;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preference, rootKey);


            Preference verPref = findPreference("version_key");
            if (verPref != null) {
                verPref.setDefaultValue(String.valueOf(BuildConfig.VERSION_CODE));
                verPref.setSummary(String.format(Locale.getDefault(), "%s - %d", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
            }
        }
    }
}
