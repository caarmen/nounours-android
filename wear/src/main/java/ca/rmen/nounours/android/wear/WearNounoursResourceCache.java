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
package ca.rmen.nounours.android.wear;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;

import ca.rmen.nounours.android.common.nounours.cache.ImageCache;
import ca.rmen.nounours.android.common.nounours.cache.NounoursResourceCache;
import ca.rmen.nounours.data.Image;
import ca.rmen.nounours.data.Theme;

public class WearNounoursResourceCache implements NounoursResourceCache {

    private final Context mContext;
    private final ImageCache mImageCache;
    private final Handler mUiHandler;

    public WearNounoursResourceCache(Context context) {
        mContext = context;
        mImageCache = new ImageCache();
        mUiHandler = new Handler();
    }
    @Override
    public boolean loadImages(Theme theme, ImageCache.ImageCacheListener imageCacheListener) {
        return mImageCache.cacheImages(mContext, theme.getImages().values(), mUiHandler, imageCacheListener);
    }

    @Override
    public Bitmap getDrawableImage(Context context, Image image) {
        return mImageCache.getDrawableImage(context, image);
    }

    @Override
    public void freeImages() {
        mImageCache.clearImageCache();
    }

    @Override
    public boolean loadSounds(Theme theme) {
        return true;
    }

    @Override
    public void freeSounds() {

    }
}
