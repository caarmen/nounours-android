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

package ca.rmen.nounours.android.handheld.nounours;

import android.content.Context;
import android.os.Vibrator;

import ca.rmen.nounours.NounoursVibrateHandler;

/**
 * Manages vibration for Nounours on the android device.
 *
 * @author Carmen Alvarez
 */
public class VibrateHandler implements NounoursVibrateHandler {

    private final Context mContext;

    public VibrateHandler(Context context) {
        this.mContext = context;
    }

    @Override
    public void doVibrate(final long duration) {
        final Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(duration);
    }

    @Override
    public void doVibrate(final long duration, final long interval) {
        final Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        final long[] pattern = new long[(int) (duration / interval)];
        for (int i = 0; i < pattern.length; i++) {
            pattern[i] = interval;
        }
        vibrator.vibrate(pattern, -1);
    }

}
