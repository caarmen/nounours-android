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

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

import ca.rmen.nounours.Constants;
import ca.rmen.nounours.NounoursAnimationHandler;
import ca.rmen.nounours.compat.BitmapCompat;
import ca.rmen.nounours.data.Animation;
import ca.rmen.nounours.data.AnimationImage;
import ca.rmen.nounours.data.Image;

/**
 * Manages the Nounours animations displayed to the Android device.
 *
 * @author Carmen Alvarez
 */
class AnimationHandler implements NounoursAnimationHandler {
    private static final String TAG = Constants.TAG + AnimationHandler.class.getSimpleName();

    private final AndroidNounours mNounours;
    private final Context mContext;
    private final ImageView mImageView;
    private static final Map<String, AnimationDrawable> mAnimationCache = new HashMap<>();

    private static final Map<Bitmap, BitmapDrawable> mBitmapDrawables = new HashMap<>();

    public AnimationHandler(Context context, AndroidNounours nounours, ImageView imageView) {
        mContext = context;
        mNounours = nounours;
        mImageView = imageView;
    }

    public void reset() {
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

    /**
     * @return true if an animation is currently active.
     * @see ca.rmen.nounours.Nounours#isAnimationRunning()
     */
    public boolean isAnimationRunning() {
        Log.v(TAG, "isAnimationRunning");
        final AnimationDrawable currentAnimation = getCurrentAnimationDrawable();
        if (currentAnimation == null) {
            Log.v(TAG, "Not running any animation");
            return false;

        }
        // It is not running
        if (!currentAnimation.isRunning()) {
            return false;
        }
        // For some reason, isRunning() can be true, even after the animation
        // has completed. The animation is stuck
        // on its last frame.
        final Drawable currentImage = currentAnimation.getCurrent();
        final Drawable lastImage = currentAnimation.getFrame(currentAnimation.getNumberOfFrames() - 1);
        // If we're stuck on the last frame, close the animation.
        if (currentImage == lastImage) {
            Log.v(TAG, "isRunning true, yet on last image?");
            currentAnimation.stop();
            return false;
        }
        Log.v(TAG, "Currently running animation");
        return true;
    }

    /**
     * @return the currently running android animation, if any.
     */
    private AnimationDrawable getCurrentAnimationDrawable() {
        final Drawable currentDrawable = mImageView.getDrawable();
        // First see if an animation is visible on the screen.
        if (currentDrawable instanceof AnimationDrawable) {
            return (AnimationDrawable) currentDrawable;
        }
        return null;
    }

    /**
     * The user selected an animation from the menu. Display the animation.
     *
     * @see ca.rmen.nounours.Nounours#doAnimation(Animation, boolean)
     */
    public void doAnimation(final Animation animation, final boolean isDynamicAnimation) {
        Log.v(TAG, "doAnimation: " + animation);
        final Runnable runnable = new Runnable() {
            public void run() {
                // Create an Android animation.
                final AnimationDrawable animationDrawable = createAnimation(animation, !isDynamicAnimation);
                if (animationDrawable == null) {
                    Log.v(TAG, "No animation " + animation.getId());
                    return;
                }

                // Display the Android animation.
                mImageView.setImageDrawable(animationDrawable);

                animationDrawable.start();
                animationDrawable.setOneShot(true);
                Log.v(TAG, "launched animation " + animation.getId());
            }
        };
        mNounours.runTask(runnable);

    }

    /**
     * Stop the currently running animation, if there is one.
     *
     * @see ca.rmen.nounours.Nounours#stopAnimation()
     */
    public void stopAnimation() {
        final AnimationDrawable animation = getCurrentAnimationDrawable();
        if (animation != null) {
            animation.stop();
        }
    }

    /**
     * Create an Android animation given the nounours animation data.
     *
     * @param doCache if true, this image sequence will be stored in memory for future use.
     */
    private AnimationDrawable createAnimation(final Animation animation, boolean doCache) {
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
                final Bitmap bitmap = mNounours.getDrawableImage(image);
                if (bitmap == null)
                    return null;

                BitmapDrawable drawable = getDrawable(bitmap);
                if (drawable == null)
                    return null;
                animationDrawable.addFrame(drawable, (int) (animation.getInterval() * animationImage.getDuration()));

            }
        }
        // Add the default image at the end.
        final Bitmap bitmap = mNounours.getDrawableImage(mNounours.getDefaultImage());
        if (bitmap == null)
            return null;
        BitmapDrawable drawable = getDrawable(bitmap);
        if (drawable == null)
            return null;
        animationDrawable.addFrame(drawable, animation.getInterval());
        Log.v(TAG, "Loaded animation " + animation.getId());

        return animationDrawable;
    }

    /**
     * Store bitmap drawables for bitmaps in cache.
     */
    private BitmapDrawable getDrawable(Bitmap bitmap) {
        BitmapDrawable result = mBitmapDrawables.get(bitmap);
        if (result != null)
            return result;
        result = BitmapCompat.createBitmapDrawable(mContext, bitmap);
        mBitmapDrawables.put(bitmap, result);
        return result;
    }

    /**
     * Store all animations in memory for performance.
     */
    boolean cacheAnimations() {
        Log.v(TAG, "cacheAnimations");
        final Map<String, Animation> animations = mNounours.getAnimations();
        for (final String animationId : animations.keySet()) {
            final Animation animation = animations.get(animationId);
            AnimationDrawable animationDrawable = createAnimation(animation, true);
            if (animationDrawable == null)
                return false;
        }
        return true;
    }

    /**
     * The implementing class may implement this to add the menu item for the
     * animation, as it is read from the CSV file. If this must be handled
     * later, the method {#link {@link ca.rmen.nounours.data.Theme#getAnimations()} may be used instead.
     */
    public void addAnimation(Animation animation) {
        // Do nothing
    }

    public void onDestroy() {
        mAnimationCache.clear();
        mBitmapDrawables.clear();
    }
}
