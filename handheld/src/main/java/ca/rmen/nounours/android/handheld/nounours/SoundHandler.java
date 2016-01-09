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
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

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

    private final Context mContext;
    private final SoundCache mSoundCache;
    private boolean mSoundEnabled = true;
    private final MediaPlayer mMediaPlayer;


    public SoundHandler(Context context, SoundCache soundCache) {
        mContext = context;
        mSoundCache = soundCache;
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnErrorListener(new MediaPlayerErrorListener());
    }

    /**
     * Play a sound.
     */
    @Override
    public void playSound(final String soundId) {
        Log.v(TAG, "playSound " + soundId);
        if (!mSoundEnabled) return;
        String assetPath = mSoundCache.getAssetPath(soundId);
        try {
            AssetFileDescriptor assetFd = mContext.getAssets().openFd(assetPath);
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(assetFd.getFileDescriptor(),
                    assetFd.getStartOffset(), assetFd.getLength());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * Stop playing a sound.
     *
     * @see ca.rmen.nounours.Nounours#stopSound()
     */
    @Override
    public void stopSound() {
        Log.v(TAG, "stopSound");
        mMediaPlayer.stop();
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
        if (enableSound) {
            mMediaPlayer.setVolume(1f, 1f);
        } else {
            mMediaPlayer.setVolume(0f, 0f);
        }
    }

    private static class MediaPlayerErrorListener implements MediaPlayer.OnErrorListener {
        /**
         * Some error occurred using the media player
         *
         * @see android.media.MediaPlayer.OnErrorListener#onError(android.media.MediaPlayer,
         * int, int)
         */
        @Override
        public boolean onError(final MediaPlayer mp, final int what, final int extra) {
            Log.v(TAG, "MediaPlayer error: MediaPlayer = " + mp + "(" + mp.getClass() + "), what=" + what
                    + ", extra = " + extra);
            return false;
        }
    }

}
