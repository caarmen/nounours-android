/*
 *   Copyright (c) 2009 - 2015 Carmen Alvarez
 *
 *   This file is part of Nounours for Android.
 *
 *   Nounours for Android is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Nounours for Android is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Nounours for Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.rmen.nounours.android.handheld.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import ca.rmen.nounours.R;
import ca.rmen.nounours.android.handheld.compat.ActivityCompat;
import ca.rmen.nounours.android.handheld.compat.ApiHelper;
import ca.rmen.nounours.android.handheld.util.ThemeUtil;


/**
 * A {@link PreferenceActivity} that presents a set of application app_settings. On
 * handset devices, app_settings are presented as a single list. On tablets,
 * app_settings are split by category, with category headers shown to the left of
 * the list of app_settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {

    private static final String EXTRA_PREFERENCE_XML_RES_ID = "nounours_preference_xml_res_id";
    private static final String PREF_LAUNCH_WALLPAPER_SETTINGS = "launch_wallpaper_settings";

    public static void startAppSettingsActivity(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.putExtra(EXTRA_PREFERENCE_XML_RES_ID, R.xml.app_settings);
        context.startActivity(intent);
    }

    @SuppressWarnings("unused")
    public static void startLwpSettingsActivity(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.putExtra(EXTRA_PREFERENCE_XML_RES_ID, R.xml.lwp_settings);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.setDisplayHomeAsUpEnabled(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        int xmlResId = getIntent().getIntExtra(EXTRA_PREFERENCE_XML_RES_ID, R.xml.lwp_settings);

        //noinspection deprecation
        addPreferencesFromResource(xmlResId);

        ListPreference themePreference = null;
        Preference backgroundColorPreference = null;

        List<Preference> preferencesToHide = new ArrayList<>();

        //noinspection deprecation
        PreferenceScreen preferenceScreen = getPreferenceScreen();

        // Hide preferences which are not relevant.
        for (int i = 0; i < preferenceScreen.getPreferenceCount(); i++) {
            Preference preference = preferenceScreen.getPreference(i);
            // Only care about preferences with keys
            if (preference.getKey() == null) continue;

            if (preference.getKey().endsWith(SharedPreferenceSettings.PREF_THEME)) {
                // If we have only one theme, there's no point in showing the theme preference.
                themePreference = (ListPreference) preference;
                if (themePreference.getEntries().length == 1) {
                    preferencesToHide.add(preference);
                }
            }
            // The wallpaper feature isn't available on older devices.
            else if (PREF_LAUNCH_WALLPAPER_SETTINGS.equals(preference.getKey())) {
                if (ApiHelper.getAPILevel() < Build.VERSION_CODES.ECLAIR_MR1) {
                    preferencesToHide.add(preference);
                }
            } else if (preference.getKey().endsWith(SharedPreferenceSettings.PREF_BACKGROUND_COLOR)) {
                // If we have no transparent themes, it doesn't make sense to have this setting.
                if (getResources().getStringArray(R.array.transparentThemes).length == 0) {
                    preferencesToHide.add(preference);
                } else {
                    backgroundColorPreference = preference;
                }
            }
        }
        for (Preference preference : preferencesToHide) {
            preferenceScreen.removePreference(preference);
        }

        if (themePreference != null) {
            ThemeChangedListener themeChangedListener = new ThemeChangedListener(this, backgroundColorPreference);
            themePreference.setOnPreferenceChangeListener(themeChangedListener);
            themeChangedListener.onPreferenceChange(themePreference, themePreference.getValue());
        }
    }

    private static void updateListPreferenceSummary(ListPreference listPreference, String newValue) {
        int index = listPreference.findIndexOfValue(newValue);
        // Set the summary to reflect the new value.
        listPreference.setSummary(listPreference.getEntries()[index]);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * When the theme changes, we need to update its summary, and enable or disable the background
     * color setting, as not all themes support this setting.
     */
    private static class ThemeChangedListener implements Preference.OnPreferenceChangeListener {

        private final Context mContext;
        private final Preference mBackgroundColorPreference;

        ThemeChangedListener(Context context, Preference backgroundColorPreference) {
            mContext = context;
            mBackgroundColorPreference = backgroundColorPreference;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String themeId = (String) newValue;
            if (mBackgroundColorPreference != null)
                mBackgroundColorPreference.setEnabled(ThemeUtil.isThemeTransparent(mContext, themeId));
            updateListPreferenceSummary((ListPreference) preference, themeId);
            return true;
        }
    }
}
