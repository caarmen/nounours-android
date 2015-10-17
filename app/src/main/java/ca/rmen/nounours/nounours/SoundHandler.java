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

package ca.rmen.nounours.nounours;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.SoundPool;
import android.util.Log;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ca.rmen.nounours.Constants;
import ca.rmen.nounours.NounoursSoundHandler;
import ca.rmen.nounours.compat.SoundPoolCompat;
import ca.rmen.nounours.data.Sound;
import ca.rmen.nounours.data.Theme;

/**
 * Manages sound effects and music for Nounours on the Android device.
 *
 * @author Carmen Alvarez
 */
class SoundHandler implements NounoursSoundHandler {
    private static final String TAG = Constants.TAG + SoundHandler.class.getSimpleName();

    private final Context mContext;
    private final SoundPool mSoundPool;
    private boolean mSoundEnabled = true;
    // We use SoundPool instead of MediaPlayer because it allows playing sounds which
    // are not on the sdcard.  But we only play one sound at a time.
    private int mCurrentSoundId;
    private final Map<String, Integer> mSoundPoolIds = new ConcurrentHashMap<>();


    public SoundHandler(Context context) {
        mContext = context;
        // Initialize the media player.
        mSoundPool = SoundPoolCompat.create(context);
    }

    public void cacheSounds(final Theme theme) {
        Log.v(TAG, "cacheSounds for theme " + theme);
        // clear the existing cache
        for (Integer soundPoolId : mSoundPoolIds.values()) {
            mSoundPool.unload(soundPoolId);
        }
        mSoundPoolIds.clear();

        new Thread(){
            @Override
            public void run() {
                for (Sound sound : theme.getSounds().values()) {
                    final int soundPoolId;
                    String assetPath = "themes/" + theme.getId() + "/" + sound.getFilename();
                    try {
                        AssetFileDescriptor assetFd = mContext.getAssets().openFd(assetPath);
                        soundPoolId = mSoundPool.load(assetFd, 0);
                    } catch (IOException e) {
                        Log.v(TAG, "couldn't load sound " + sound, e);
                        continue;
                    }
                    mSoundPoolIds.put(sound.getId(), soundPoolId);
                }
                Log.v(TAG, "cached sounds");
            }
        }.start();
    }

    /**
     * Play a sound.
     */
    public void playSound(final String soundId) {
        Log.v(TAG, "playSound " + soundId);
        if (!mSoundEnabled) return;
        int soundPoolId = mSoundPoolIds.get(soundId);
        mCurrentSoundId = mSoundPool.play(soundPoolId, 1.0f, 1.0f, 0, 0, 1.0f);
        Log.v(TAG, "sound play result for " + soundPoolId + ": " + mCurrentSoundId);
    }

    /**
     * Stop playing a sound.
     *
     * @see ca.rmen.nounours.Nounours#stopSound()
     */
    public void stopSound() {
        Log.v(TAG, "stopSound");
        mSoundPool.stop(mCurrentSoundId);
    }

    /**
     * Mute or unmute the media player.
     *
     * @see ca.rmen.nounours.Nounours#setEnableSound(boolean)
     */
    public void setEnableSound(final boolean enableSound) {
        mSoundEnabled = enableSound;
    }

}
