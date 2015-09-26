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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ca.rmen.nounours.Constants;
import ca.rmen.nounours.compat.EnvironmentCompat;
import ca.rmen.nounours.data.Image;
import ca.rmen.nounours.util.BitmapUtil;

class ImageCache {

    interface ImageCacheListener {
        void onImageLoaded(Image image, int progress, int total);
    }

    private static final String TAG = Constants.TAG + ImageCache.class.getSimpleName();

    private final Map<String, Bitmap> mImageCache = new ConcurrentHashMap<>();
    private final Context mContext;
    private final ImageCacheListener mListener;

    ImageCache(Context context, ImageCacheListener listener) {
        mContext = context;
        mListener = listener;
    }

    /**
     * Load the images into memory.
     */
    boolean cacheImages(Collection<Image> images) {
        int i = 0;
        int max = images.size();
        for (final Image image : images) {
            Bitmap bitmap = loadImage(image);
            if (bitmap == null)
                return false;
            mListener.onImageLoaded(image, i++, max);
        }
        return true;
    }

    void clearImageCache() {

        for (Bitmap bitmap : mImageCache.values()) {
            if (!bitmap.isRecycled()) bitmap.recycle();
        }
        mImageCache.clear();
        System.gc();

    }

    /**
     * Find the Android image for the given nounours image.
     */
    Bitmap getDrawableImage(final Image image) {
        Bitmap res = mImageCache.get(image.getId());
        if (res == null) {
            Log.v(TAG, "Loading drawable image " + image);
            res = loadImage(image);
        }
        return res;
    }

    /**
     * Load an image from the disk into memory. Return the Drawable for the
     * image.
     */
    private Bitmap loadImage(final Image image) {
        Log.v(TAG, "Loading " + image + " into memory");
        // This is one of the downloaded images, in the sdcard.
        if (image.getFilename().contains(EnvironmentCompat.getExternalFilesDir(mContext).getAbsolutePath())) {
            // Load the new image
            Log.v(TAG, "Load themed image.");
            Bitmap newBitmap = BitmapUtil.loadBitmap(mContext, image.getFilename());
            return copyAndCacheImage(newBitmap, image.getId());
        }
        // This is one of the default images bundled in the apk.
        else {
            final int imageResId = mContext.getResources().getIdentifier(image.getFilename(), "drawable",
                    mContext.getClass().getPackage().getName());
            // Load the image from the resource file.
            Log.v(TAG, "Load default image " + imageResId);
            Bitmap readOnlyBitmap = BitmapUtil.loadBitmap(mContext, imageResId);
            Log.v(TAG, "default image mutable = " + readOnlyBitmap.isMutable() + ", recycled="
                    + readOnlyBitmap.isRecycled());
            // Store the newly loaded drawable in cache for the first time.
            // Make a mutable copy of the drawable.
            return copyAndCacheImage(readOnlyBitmap, image.getId());
        }
    }

    /**
     * Create a mutable copy of the given immutable bitmap, and store it in the
     * cache.
     *
     * @param readOnlyBitmap the immutable bitmap
     * @return the mutable copy of the read-only bitmap.
     */
    private Bitmap copyAndCacheImage(Bitmap readOnlyBitmap, String imageId) {
        Bitmap mutableBitmap = readOnlyBitmap.copy(readOnlyBitmap.getConfig(), true);
        Canvas canvas = new Canvas(mutableBitmap);
        canvas.drawBitmap(readOnlyBitmap, 0, 0, null);
        readOnlyBitmap.recycle();
        synchronized (mImageCache) {
            mImageCache.put(imageId, mutableBitmap);
        }
        return mutableBitmap;
    }

}
