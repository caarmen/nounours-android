package ca.rmen.nounours;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by calvarez on 27/08/15.
 */
final class AndroidNounoursSettings {
    private AndroidNounoursSettings() {
        // Prevent instantiation
    }

    static boolean isSoundEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(AndroidNounours.PREF_SOUND_AND_VIBRATE, true);
    }

    static boolean isRandomAnimationEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(AndroidNounours.PREF_RANDOM, true);
    }

    static long getIdleTimeout(Context context) {
        return Long.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(AndroidNounours.PREF_IDLE_TIMEOUT, "30000"));
    }

    static void setSoundEnabled(Context context, boolean enabled) {

    }
}
