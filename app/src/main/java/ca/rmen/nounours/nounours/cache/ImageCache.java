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
package ca.rmen.nounours.nounours.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ca.rmen.nounours.Constants;
import ca.rmen.nounours.data.Image;
import ca.rmen.nounours.util.BitmapUtil;

public class ImageCache {


    private static final String TAG = Constants.TAG + ImageCache.class.getSimpleName();

    private final Map<String, Bitmap> mImageCache = new ConcurrentHashMap<>();

    public interface ImageCacheListener {
        void onImageLoaded(Image image, int progress, int total);
    }

    public ImageCache() {
        Log.v(TAG, "Constructor");
    }

    /**
     * Load the images into memory.
     */
    public boolean cacheImages(Context context, Collection<Image> images, Handler uiHandler, final ImageCacheListener listener) {
        Log.v(TAG, "cacheImages");
        int i = 0;
        final int max = images.size();
        for (final Image image : images) {
            Bitmap bitmap = loadImage(context, image);
            if (bitmap == null)
                return false;
            i++;
            final int progress = i;
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onImageLoaded(image, progress, max);
                }
            });
        }
        return true;
    }

    public void clearImageCache() {
        Log.v(TAG, "clearImageCache");

        for (Bitmap bitmap : mImageCache.values()) {
            if (!bitmap.isRecycled()) bitmap.recycle();
        }
        mImageCache.clear();
        System.gc();

    }

    /**
     * Find the Android image for the given nounours image.
     */
    public Bitmap getDrawableImage(Context context, final Image image) {
        Bitmap res = mImageCache.get(image.getId());
        if (res == null) {
            Log.v(TAG, "Loading drawable image " + image);
            res = loadImage(context, image);
        }
        return res;
    }


    /**
     * Load an image from the disk into memory. Return the Drawable for the
     * image.
     */
    private Bitmap loadImage(Context context, final Image image) {
        Log.v(TAG, "Loading " + image + " into memory");
        Bitmap result = BitmapUtil.createBitmap(context, image);
        if (result != null) mImageCache.put(image.getId(), result);
        return result;
    }


}
