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

import ca.rmen.nounours.data.Image;
import ca.rmen.nounours.data.Theme;

/**
 * Responsible for caching and freeing the image and sound resources needed by Nounours.
 */
public interface NounoursResourceCache {

    /**
     * @param theme load the images from this theme
     * @param imageCacheListener notify this listener as each image is loaded
     * @return true if the images could all be loaded, false otherwise.
     */
    boolean loadImages(Theme theme, ImageCache.ImageCacheListener imageCacheListener);
    Bitmap getDrawableImage(Context context, final Image image);
    void freeImages();

    boolean loadSounds(Theme theme);
    void freeSounds();
}
