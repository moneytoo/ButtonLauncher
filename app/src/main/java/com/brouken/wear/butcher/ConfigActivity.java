package com.brouken.wear.butcher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.util.List;

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

        SharedPreferences mSharedPreferences;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs_config);

            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

            setupPref("home_default");
            setupPref("home_button1");
            setupPref("home_button1long");
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (resultCode == RESULT_OK) {
                String pref = data.getStringExtra("pref");
                String app = data.getStringExtra("app");
                String pkg = data.getStringExtra("pkg");
                String cls = data.getStringExtra("cls");

                savePreferences(pref, app);
                String name = getAppLabel(pkg, cls);

                updateSummary(pref, name);
            }

        }

        private void updateSummary(String pref, String name) {
            Preference preference = findPreference(pref);
            if (name == null)
                name = "None";
            preference.setSummary(name);
        }

        private void setupPref(final String pref) {
            Preference preference = findPreference(pref);

            String app = loadValue(pref);
            String summary = null;

            if (app != null) {
                String pkg = app.split("/")[0];
                String cls = app.split("/")[1];

                summary = getAppLabel(pkg, cls);
            }

            updateSummary(pref, summary);

            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    Intent intent = new Intent(getActivity(), AppPickerActivity.class);
                    intent.putExtra("pref", pref);
                    startActivityForResult(intent, 0);

                    return true;
                }
            });
        }

        private String loadValue(String key) {
            return mSharedPreferences.getString(key, null);
        }

        public void savePreferences(String key, String value) {
            if (value == null)
                return;

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(key, value);
            editor.commit();
        }

        private String getAppLabel(String pkg, String cls) {
            if (pkg == null || cls == null)
                return null;

            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            ComponentName componentName = new ComponentName(pkg, cls);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            mainIntent.setComponent(componentName);
            List<ResolveInfo> pkgAppsList = getContext().getPackageManager().queryIntentActivities( mainIntent, 0);
            return pkgAppsList.get(0).activityInfo.loadLabel(getContext().getPackageManager()).toString();
        }
    }


}

