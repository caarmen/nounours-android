/*
 * Copyright (c) 2015 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours.settings;

import android.content.Context;
import android.preference.PreferenceManager;

import ca.rmen.nounours.Nounours;

public final class NounoursSettings {
    static final String PREF_THEME = "Theme";
    static final String PREF_SOUND_AND_VIBRATE = "SoundAndVibrate";
    static final String PREF_RANDOM = "Random";
    static final String PREF_IDLE_TIMEOUT = "IdleTimeout";

    private NounoursSettings() {
        // Prevent instantiation
    }

    public static boolean isSoundEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_SOUND_AND_VIBRATE, true);
    }

    public static boolean isRandomAnimationEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_RANDOM, true);
    }

    public static long getIdleTimeout(Context context) {
        return Long.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_IDLE_TIMEOUT, "30000"));
    }

    public static String getThemeId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_THEME, Nounours.DEFAULT_THEME_ID);
    }

    public static void setThemeId(Context context, String themeId) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_THEME, themeId).commit();
    }
}
