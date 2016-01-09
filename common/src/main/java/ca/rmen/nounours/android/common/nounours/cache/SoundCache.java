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

package ca.rmen.nounours.android.common.nounours.cache;

import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ca.rmen.nounours.android.common.Constants;
import ca.rmen.nounours.data.Sound;
import ca.rmen.nounours.data.Theme;

/**
 * @author Carmen Alvarez
 */
public class SoundCache {
    private static final String TAG = Constants.TAG + SoundCache.class.getSimpleName();

    private final Map<String, String> mAssetPaths = new ConcurrentHashMap<>();

    public String getAssetPath(String soundId) {
        return mAssetPaths.get(soundId);
    }

    public void cacheSounds(final Theme theme) {
        Log.v(TAG, "cacheSounds for theme " + theme);
        for (Sound sound : theme.getSounds().values()) {
            String assetPath = "themes/" + theme.getId() + "/" + sound.getFilename();
            mAssetPaths.put(sound.getId(), assetPath);
        }
        Log.v(TAG, "cached sounds");
    }

    public void clearSoundCache() {
        Log.v(TAG, "clearSoundCache");
        // clear the existing cache
        mAssetPaths.clear();
    }

}
