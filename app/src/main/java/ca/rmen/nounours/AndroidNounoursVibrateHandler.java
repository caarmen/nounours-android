/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;

/**
 * Manages vibration for Nounours on the android device.
 *
 * @author Carmen Alvarez
 */
class AndroidNounoursVibrateHandler implements NounoursVibrateHandler {

    private Activity activity = null;

    public AndroidNounoursVibrateHandler(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void doVibrate(final long duration) {
        final Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(duration);
    }

    @Override
    public void doVibrate(final long duration, final long interval) {
        final Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        final long[] pattern = new long[(int) (duration / interval)];
        for (int i = 0; i < pattern.length; i++) {
            pattern[i] = interval;
        }
        vibrator.vibrate(pattern, -1);
    }

}
