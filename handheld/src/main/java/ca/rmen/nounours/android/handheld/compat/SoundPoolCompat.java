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

package ca.rmen.nounours.android.handheld.compat;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

public class SoundPoolCompat {

    public static SoundPool create() {
        if (ApiHelper.getAPILevel() < Build.VERSION_CODES.LOLLIPOP) {
            //noinspection deprecation
            return new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        } else {
            return Api21Helper.createSoundPool();
        }
    }
}
