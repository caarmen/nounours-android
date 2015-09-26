/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours.nounours;

import android.content.Context;
import android.os.Vibrator;

import ca.rmen.nounours.NounoursVibrateHandler;

/**
 * Manages vibration for Nounours on the android device.
 *
 * @author Carmen Alvarez
 */
class VibrateHandler implements NounoursVibrateHandler {

    private Context context = null;

    public VibrateHandler(Context context) {
        this.context = context;
    }

    @Override
    public void doVibrate(final long duration) {
        final Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(duration);
    }

    @Override
    public void doVibrate(final long duration, final long interval) {
        final Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        final long[] pattern = new long[(int) (duration / interval)];
        for (int i = 0; i < pattern.length; i++) {
            pattern[i] = interval;
        }
        vibrator.vibrate(pattern, -1);
    }

}
