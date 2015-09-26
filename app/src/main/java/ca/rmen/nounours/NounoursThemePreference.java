/*
 * Copyright (c) 2015 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.ListPreference;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import ca.rmen.nounours.data.Theme;
import ca.rmen.nounours.io.ThemeReader;
import ca.rmen.nounours.util.FileUtil;
import ca.rmen.nounours.util.Trace;

public final class NounoursThemePreference {
    private static final class ThemePreferenceData {
        private final CharSequence[] entries;
        private final CharSequence[] entryValues;

        private ThemePreferenceData(CharSequence[] entries, CharSequence[] entryValues) {
            this.entries = entries;
            this.entryValues = entryValues;
        }
    }

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
                        entries[index] = AndroidNounours.getThemeLabel(context, theme);
                        entryValues[index++] = themeId;
                    }
                    return new ThemePreferenceData(entries, entryValues);
                } catch (IOException e) {
                    Trace.debug(this, "Could not read the list of themes");
                    Trace.debug(this, e);
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
