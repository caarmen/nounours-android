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
import android.preference.PreferenceManager;

public final class NounoursSettings {
    static final String PREF_THEME = "Theme";
    private static final String PREF_SOUND_AND_VIBRATE = "SoundAndVibrate";
    // IdleTimeout changed from 1.3.5 to 2.0.0 from a Long to a String
    // We just rename the preference here and don't care about migrating this setting.
    static final String PREF_IDLE_TIMEOUT = "IdleTimeout2";

    private NounoursSettings() {
        // Prevent instantiation
    }

    public static boolean isSoundEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_SOUND_AND_VIBRATE, true);
    }

    public static long getIdleTimeout(Context context) {
        return Long.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_IDLE_TIMEOUT, "30000"));
    }

    public static String getThemeId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_THEME, "0");
    }

}
