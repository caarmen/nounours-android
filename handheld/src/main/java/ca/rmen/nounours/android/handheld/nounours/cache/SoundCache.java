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

package ca.rmen.nounours.android.handheld.nounours.cache;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.SoundPool;
import android.util.Log;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ca.rmen.nounours.android.common.Constants;
import ca.rmen.nounours.data.Sound;
import ca.rmen.nounours.data.Theme;

/**
 * Loads sounds
 *
 * @author Carmen Alvarez
 */
public class SoundCache {
    private static final String TAG = Constants.TAG + SoundCache.class.getSimpleName();

    private final Context mContext;
    private final SoundPool mSoundPool;
    private final Map<String, Integer> mSoundPoolIds = new ConcurrentHashMap<>();


    public SoundCache(Context context, SoundPool soundPool) {
        mContext = context;
        // Initialize the media player.
        mSoundPool = soundPool;
    }

    public Integer getSoundPoolId(String soundId) {
        return mSoundPoolIds.get(soundId);
    }

    public void cacheSounds(final Theme theme) {
        Log.v(TAG, "cacheSounds for theme " + theme);

        Thread thread = new Thread(){
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
        };
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    public void clearSoundCache() {
        Log.v(TAG, "clearSoundCache");
        // clear the existing cache
        for (Integer soundPoolId : mSoundPoolIds.values()) {
            mSoundPool.unload(soundPoolId);
        }
        mSoundPoolIds.clear();
    }

}
