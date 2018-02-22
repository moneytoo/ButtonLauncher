package com.brouken.wear.butcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

public class ConfigActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new ConfigPrefFragment())
                .commit();
    }

    public static class ConfigPrefFragment extends PreferenceFragment {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs_config);

            Preference preferenceFontSize = findPreference("pref_font_size");
            preferenceFontSize.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    Intent intent = new Intent(getActivity(), AppPickerActivity.class);
                    startActivity(intent);

                    getActivity().finish();
                    return true;
                }
            });
        }
    }
}

