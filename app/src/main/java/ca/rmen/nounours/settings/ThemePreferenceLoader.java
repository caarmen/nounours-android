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

package ca.rmen.nounours.settings;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.ListPreference;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import ca.rmen.nounours.Constants;
import ca.rmen.nounours.Nounours;
import ca.rmen.nounours.R;
import ca.rmen.nounours.data.Theme;
import ca.rmen.nounours.io.ThemeReader;
import ca.rmen.nounours.util.FileUtil;
import ca.rmen.nounours.util.ThemeUtil;

public final class ThemePreferenceLoader {
    private static final class ThemePreferenceData {
        final CharSequence[] entries;
        final CharSequence[] entryValues;

        private ThemePreferenceData(CharSequence[] entries, CharSequence[] entryValues) {
            this.entries = entries;
            this.entryValues = entryValues;
        }
    }

    private static final String TAG = Constants.TAG + ThemePreferenceLoader.class.getSimpleName();

    static void load(final Context context, final ListPreference listPreference) {

        listPreference.setEnabled(false);
        if (!FileUtil.isSdPresent()) return;

        new AsyncTask<Void, Void, ThemePreferenceData>() {
            @Override
            protected ThemePreferenceData doInBackground(Void... params) {
                InputStream themeFile = context.getResources().openRawResource(R.raw.imageset);
                try {
                    ThemeReader themeReader = new ThemeReader(themeFile);
                    Map<String, Theme> themes = themeReader.getThemes();
                    SortedSet<String> sortedThemeList = new TreeSet<>();
                    sortedThemeList.addAll(themes.keySet());
                    CharSequence[] entries = new CharSequence[themes.size() + 1];
                    CharSequence[] entryValues = new CharSequence[themes.size() + 1];
                    entryValues[0] = Nounours.DEFAULT_THEME_ID;
                    entries[0] = context.getString(R.string.defaultTheme);
                    int index = 1;
                    for (String themeId : sortedThemeList) {
                        Theme theme = themes.get(themeId);
                        entries[index] = ThemeUtil.getThemeLabel(context, theme);
                        entryValues[index++] = themeId;
                    }
                    return new ThemePreferenceData(entries, entryValues);
                } catch (IOException e) {
                    Log.v(TAG, "Could not read the list of themes", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(ThemePreferenceData themePreferenceData) {
                listPreference.setEntries(themePreferenceData.entries);
                listPreference.setEntryValues(themePreferenceData.entryValues);

                listPreference.setSummary(listPreference.getEntry());
                listPreference.setEnabled(true);
            }
        }.execute();

    }
}
