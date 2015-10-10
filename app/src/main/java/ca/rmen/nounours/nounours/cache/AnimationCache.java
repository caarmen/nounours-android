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
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ca.rmen.nounours.Constants;
import ca.rmen.nounours.compat.BitmapCompat;
import ca.rmen.nounours.data.Animation;
import ca.rmen.nounours.data.AnimationImage;
import ca.rmen.nounours.data.Image;

/**
 * @author Carmen Alvarez
 */
public class AnimationCache {
    private static final String TAG = Constants.TAG + AnimationCache.class.getSimpleName();

    private final Map<String, AnimationDrawable> mAnimationCache = new HashMap<>();
    private final Map<Bitmap, BitmapDrawable> mBitmapDrawables = new HashMap<>();
    private final ImageCache mImageCache;

    public AnimationCache(ImageCache imageCache) {
        Log.v(TAG, "Constructor");
        mImageCache = imageCache;
    }

    /**
     * Store all animations in memory for performance.
     */
    public boolean cacheAnimations(Context context, Collection<Animation> animations, Image defaultImage) {
        Log.v(TAG, "cacheAnimations");
        for (final Animation animation : animations) {
            AnimationDrawable animationDrawable = createAnimation(context, animation, defaultImage, true);
            if (animationDrawable == null) return false;
        }
        return true;
    }

    /**
     * @return an Android animation given the nounours animation data.
     *
     * @param doCache if true, this image sequence will be stored in memory for future use.
     */
    public AnimationDrawable createAnimation(Context context, final Animation animation, Image defaultImage, boolean doCache) {
        Log.v(TAG, "createAnimation " + animation + " doCache = " + doCache);
        // First see if we have this stored in memory.
        AnimationDrawable animationDrawable = mAnimationCache.get(animation.getId());
        if (animationDrawable != null) {
            return animationDrawable;
        }

        // Create the android animation.
        animationDrawable = createAnimation(context, animation);
        if (doCache)
            mAnimationCache.put(animation.getId(), animationDrawable);
        // Add the default image at the end.
        BitmapDrawable drawable = getDrawable(context, defaultImage);
        animationDrawable.addFrame(drawable, animation.getInterval());
        Log.v(TAG, "Loaded animation " + animation.getId());

        return animationDrawable;
    }

    /**
     * @return an Android animation for the given nounours animation data. This animation will not be cached for future use.
     */
    public AnimationDrawable createAnimation(Context context, Animation animation) {
        AnimationDrawable animationDrawable = new AnimationDrawable();
        // Go through the list of images in the nounours animation, "repeat"
        // times.
        for (int i = 0; i < animation.getRepeat(); i++) {
            for (final AnimationImage animationImage : animation.getImages()) {
                // Get the android image and add it to the android animation.
                BitmapDrawable drawable = getDrawable(context, animationImage.getImage());
                animationDrawable.addFrame(drawable, (int) (animation.getInterval() * animationImage.getDuration()));
            }
        }
        return animationDrawable;
    }

    /**
     * Store bitmap drawables for bitmaps in cache.
     */
    private BitmapDrawable getDrawable(Context context, Image image) {
        final Bitmap bitmap = mImageCache.getDrawableImage(context, image);
        BitmapDrawable result = mBitmapDrawables.get(bitmap);
        if (result != null) return result;
        result = BitmapCompat.createBitmapDrawable(context, bitmap);
        mBitmapDrawables.put(bitmap, result);
        return result;
    }


    public void clearAnimationCache() {
        for (AnimationDrawable animationDrawable : mAnimationCache.values()) {
            purgeAnimationDrawable(animationDrawable);
        }
        mAnimationCache.clear();
        for (Bitmap bitmap : mBitmapDrawables.keySet()) {
            if (bitmap != null && !bitmap.isRecycled())
                bitmap.recycle();
        }
        mBitmapDrawables.clear();
    }

    private void purgeAnimationDrawable(AnimationDrawable animationDrawable) {
        animationDrawable.stop();
        for (int i = 0; i < animationDrawable.getNumberOfFrames(); i++) {
            Drawable frame = animationDrawable.getFrame(i);
            if (frame instanceof BitmapDrawable) {
                BitmapDrawable bmDrawable = (BitmapDrawable) frame;
                Bitmap bitmap = bmDrawable.getBitmap();
                if (bitmap != null && !bitmap.isRecycled())
                    bitmap.recycle();
            }
        }
    }
}
