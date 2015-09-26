/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
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
import ca.rmen.nounours.nounours.AndroidNounours;

/**
 * @author Carmen Alvarez
 */
public class AnimationCache {
    private static final String TAG = Constants.TAG + AnimationCache.class.getSimpleName();

    private final AndroidNounours mNounours;
    private final Context mContext;
    private final ImageCache mImageCache;

    private final Map<String, AnimationDrawable> mAnimationCache = new HashMap<>();
    private final Map<Bitmap, BitmapDrawable> mBitmapDrawables = new HashMap<>();

    public AnimationCache(Context context, AndroidNounours nounours, ImageCache imageCache) {
        mContext = context;
        mNounours = nounours;
        mImageCache = imageCache;
    }

    /**
     * Store all animations in memory for performance.
     */
    public boolean cacheAnimations(Collection<Animation> animations) {
        Log.v(TAG, "cacheAnimations");
        for (final Animation animation : animations) {
            AnimationDrawable animationDrawable = createAnimation(animation, true);
            if (animationDrawable == null) return false;
        }
        return true;
    }

    /**
     * Create an Android animation given the nounours animation data.
     *
     * @param doCache if true, this image sequence will be stored in memory for future use.
     */
    public AnimationDrawable createAnimation(final Animation animation, boolean doCache) {
        Log.v(TAG, "createAnimation " + animation + " doCache = " + doCache);
        // First see if we have this stored in memory.
        AnimationDrawable animationDrawable = mAnimationCache.get(animation.getId());
        if (animationDrawable != null) {
            return animationDrawable;
        }

        // Create the android animation.
        animationDrawable = new AnimationDrawable();
        if (doCache)
            mAnimationCache.put(animation.getId(), animationDrawable);

        // Go through the list of images in the nounours animation, "repeat"
        // times.
        for (int i = 0; i < animation.getRepeat(); i++) {
            for (final AnimationImage animationImage : animation.getImages()) {
                // Make sure the image exists.
                final Image image = mNounours.getImages().get(animationImage.getImageId());
                if (image == null) {
                    Log.v(TAG, "No image " + animationImage);
                    return null;
                }
                // Get the android image and add it to the android animation.
                final Bitmap bitmap = mImageCache.getDrawableImage(image);

                BitmapDrawable drawable = getDrawable(bitmap);
                animationDrawable.addFrame(drawable, (int) (animation.getInterval() * animationImage.getDuration()));
            }
        }
        // Add the default image at the end.
        final Bitmap bitmap = mImageCache.getDrawableImage(mNounours.getDefaultImage());
        BitmapDrawable drawable = getDrawable(bitmap);
        animationDrawable.addFrame(drawable, animation.getInterval());
        Log.v(TAG, "Loaded animation " + animation.getId());

        return animationDrawable;
    }

    /**
     * Store bitmap drawables for bitmaps in cache.
     */
    private BitmapDrawable getDrawable(Bitmap bitmap) {
        BitmapDrawable result = mBitmapDrawables.get(bitmap);
        if (result != null) return result;
        result = BitmapCompat.createBitmapDrawable(mContext, bitmap);
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