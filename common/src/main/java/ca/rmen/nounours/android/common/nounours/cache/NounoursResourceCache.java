/*
 *   Copyright (c) 2015 Carmen Alvarez
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

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import ca.rmen.nounours.android.common.Constants;
import ca.rmen.nounours.android.common.settings.NounoursSettings;
import ca.rmen.nounours.data.Image;
import ca.rmen.nounours.data.Theme;

/**
 * Responsible for caching and freeing the image and sound resources needed by Nounours.
 */
public class NounoursResourceCache {
    private static final String TAG = Constants.TAG + NounoursResourceCache.class.getSimpleName();

    private final Context mContext;
    private final Handler mUiHandler;

    private final NounoursSettings mSettings;

    private final ImageCache mImageCache;
    private final SoundCache mSoundCache;


    public NounoursResourceCache(Context context,
                                         NounoursSettings settings,
                                         ImageCache imageCache,
                                         SoundCache soundCache) {
        mContext = context;
        mUiHandler = new Handler();
        mSettings = settings;
        mImageCache = imageCache;
        mSoundCache = soundCache;
    }

    public NounoursResourceCache(Context context, NounoursSettings settings, ImageCache imageCache) {
        this(context, settings, imageCache, null);
    }

    public boolean loadImages(Theme theme, ImageCache.ImageCacheListener imageCacheListener) {
        Log.v(TAG, "loadImages, theme = " + theme);
        return mImageCache.cacheImages(mContext, theme.getImages().values(), mUiHandler, imageCacheListener);
    }

    public Bitmap getDrawableImage(Context context, Image image) {
        return mImageCache.getDrawableImage(context, image);
    }

    public void freeImages() {
        Log.v(TAG, "freeImages");
        mImageCache.clearImageCache();
    }

    public boolean loadSounds(Theme theme) {
        Log.v(TAG, "loadSounds, theme = " + theme);
        if (mSoundCache != null && mSettings.isSoundEnabled()) mSoundCache.cacheSounds(theme);
        return true;
    }

    public void freeSounds() {
        Log.v(TAG, "freeSounds");
        if (mSoundCache != null) mSoundCache.clearSoundCache();
    }

}
