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
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import ca.rmen.nounours.Constants;
import ca.rmen.nounours.NounoursAnimationHandler;
import ca.rmen.nounours.data.Animation;

/**
 * Manages the Nounours animations displayed to the Android device.
 *
 * @author Carmen Alvarez
 */
class AnimationHandler implements NounoursAnimationHandler {
    private static final String TAG = Constants.TAG + AnimationHandler.class.getSimpleName();

    private final AndroidNounours mNounours;
    private final ImageView mImageView;
    private final AnimationCache mAnimationCache;

    public AnimationHandler(AndroidNounours nounours, ImageView imageView, AnimationCache animationCache) {
        mNounours = nounours;
        mImageView = imageView;
        mAnimationCache = animationCache;
    }

    /**
     * @return true if an animation is currently active.
     * @see ca.rmen.nounours.Nounours#isAnimationRunning()
     */
    @Override
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
    @Override
    public void doAnimation(final Animation animation, final boolean isDynamicAnimation) {
        Log.v(TAG, "doAnimation: " + animation);
        final Runnable runnable = new Runnable() {
            public void run() {
                // Create an Android animation.
                final AnimationDrawable animationDrawable = mAnimationCache.createAnimation(animation, !isDynamicAnimation);
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
    @Override
    public void stopAnimation() {
        final AnimationDrawable animation = getCurrentAnimationDrawable();
        if (animation != null) {
            animation.stop();
        }
    }

    /**
     * The implementing class may implement this to add the menu item for the
     * animation, as it is read from the CSV file. If this must be handled
     * later, the method {#link {@link ca.rmen.nounours.data.Theme#getAnimations()} may be used instead.
     */
    @Override
    public void addAnimation(Animation animation) {
        // Do nothing
    }

}
