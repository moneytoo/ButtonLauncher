package com.brouken.wear.butcher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.wearable.input.WearableButtons;
import android.view.KeyEvent;

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

            setupPrefs();
            setupPrefTimeout();
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

        private void setupPrefs() {
            int buttonCount = WearableButtons.getButtonCount(getContext());

            if (buttonCount >= 1) {
                setupPref("home", "default", -1);
                setupPref("home", "button0long", 0);
            }

            if (buttonCount >= 2) {
                setupPref("home", "button1", 1);
                setupPref("home", "button1long", 1);

                setupPref("extra", "default", -1);
                setupPref("extra", "button0long", 0);
                setupPref("extra", "button1", 1);
                setupPref("extra", "button1long", 1);
            }

            if (buttonCount >= 3) {
                setupPref("home", "button2", 2);
                setupPref("home", "button2long", 2);

                setupPref("extra", "button2", 2);
                setupPref("extra", "button2long", 2);
            }

            if (buttonCount >= 4) {
                setupPref("home", "button3", 3);
                setupPref("home", "button3long", 3);

                setupPref("extra", "button3", 3);
                setupPref("extra", "button3long", 3);
            }
        }

        private void setupPref(String shortcutTrigger, String shortcutAdditional, int buttonIcon) {

            final String pref = shortcutTrigger + "_" + shortcutAdditional;

            PreferenceCategory categoryHome = (PreferenceCategory) findPreference(shortcutTrigger);
            Preference preference = new Preference(getContext());

            preference.setKey(pref);
            if (shortcutAdditional.equals("default"))
                preference.setTitle("Default action");
            else if (shortcutAdditional.endsWith("long"))
                preference.setTitle("+ Long press");
            else
                preference.setTitle("+ Short press");

            categoryHome.addPreference(preference);

            String app = loadValue(pref);
            String summary = null;

            if (app != null) {
                String pkg = app.split("/")[0];
                String cls = app.split("/")[1];

                summary = getAppLabel(pkg, cls);
            }

            preference.setIcon(getIconForButton(buttonIcon));

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

        private Drawable getIconForButton(int button) {
            Drawable icon = null;
            Drawable background = ContextCompat.getDrawable(getContext(), R.drawable.ic_background);

            if (button >= 0) {
                try {
                    icon = WearableButtons.getButtonIcon(getContext(), KeyEvent.KEYCODE_STEM_PRIMARY + button);
                } catch (Exception e) {
                }
            } else
                icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_circle);

            LayerDrawable finalDrawable = new LayerDrawable(new Drawable[] {background, icon});
            int inset = (int)(icon.getIntrinsicWidth() / 5f * 2f);
            finalDrawable.setLayerInset(1, inset, inset, inset, inset);

            return finalDrawable;
        }

        private String loadValue(String key) {
            return mSharedPreferences.getString(key, null);
        }

        public void savePreferences(String key, String value) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            if (value == null)
                editor.remove(key);
            else
                editor.putString(key, value);
            editor.apply();
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

        private void setupPrefTimeout() {
            Preference preference = findPreference("timeout");

            /*preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {

                    return true;
                }
            });*/

            preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager.getDefaultSharedPreferences(getContext()).getString(preference.getKey(), "3000"));
        }

        private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                String stringValue = value.toString();

                if (preference instanceof ListPreference) {
                    // For list preferences, look up the correct display value in
                    // the preference's 'entries' list.
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(stringValue);

                    // Set the summary to reflect the new value.
                    preference.setSummary(
                            index >= 0
                                    ? listPreference.getEntries()[index]
                                    : null);

                } else {
                    // For all other preferences, set the summary to the value's
                    // simple string representation.
                    preference.setSummary(stringValue);
                }
                return true;
            }
        };
    }


}

