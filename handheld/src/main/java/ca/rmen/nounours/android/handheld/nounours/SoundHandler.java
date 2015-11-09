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

import android.media.SoundPool;
import android.util.Log;

import ca.rmen.nounours.NounoursSoundHandler;
import ca.rmen.nounours.android.common.Constants;
import ca.rmen.nounours.android.common.nounours.cache.SoundCache;

/**
 * Manages sound effects and music for Nounours on the Android device.
 *
 * @author Carmen Alvarez
 */
public class SoundHandler implements NounoursSoundHandler {
    private static final String TAG = Constants.TAG + SoundHandler.class.getSimpleName();

    private final SoundCache mSoundCache;
    private final SoundPool mSoundPool;
    private boolean mSoundEnabled = true;
    private int mCurrentSoundId;


    public SoundHandler(SoundCache soundCache, SoundPool soundPool) {
        mSoundCache = soundCache;
        mSoundPool = soundPool;
    }

    /**
     * Play a sound.
     */
    @Override
    public void playSound(final String soundId) {
        Log.v(TAG, "playSound " + soundId);
        if (!mSoundEnabled) return;
        Integer soundPoolId = mSoundCache.getSoundPoolId(soundId);
        if (soundPoolId == null) return;
        mCurrentSoundId = mSoundPool.play(soundPoolId, 1.0f, 1.0f, 0, 0, 1.0f);
        Log.v(TAG, "sound play result for " + soundPoolId + ": " + mCurrentSoundId);
    }

    /**
     * Stop playing a sound.
     *
     * @see ca.rmen.nounours.Nounours#stopSound()
     */
    @Override
    public void stopSound() {
        Log.v(TAG, "stopSound");
        mSoundPool.stop(mCurrentSoundId);
        Log.v(TAG, "stopSound finished");
    }

    /**
     * Mute or unmute the media player.
     *
     * @see ca.rmen.nounours.Nounours#setEnableSound(boolean)
     */
    @Override
    public void setEnableSound(final boolean enableSound) {
        mSoundEnabled = enableSound;
    }

}
