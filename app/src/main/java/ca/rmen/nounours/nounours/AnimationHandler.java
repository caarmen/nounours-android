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

import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

import ca.rmen.nounours.Constants;
import ca.rmen.nounours.NounoursAnimationHandler;
import ca.rmen.nounours.data.Animation;
import ca.rmen.nounours.data.AnimationImage;

/**
 * Manages the Nounours animations displayed to the Android device.
 *
 * @author Carmen Alvarez
 */
class AnimationHandler implements NounoursAnimationHandler {
    private static final String TAG = Constants.TAG + AnimationHandler.class.getSimpleName();

    private final AndroidNounours mNounours;
    private final AtomicBoolean mIsDoingAnimation = new AtomicBoolean();
    private final Handler mBackgroundHandler;
    private final AnimationTask mAnimationTask;

    public AnimationHandler(AndroidNounours nounours) {
        mNounours = nounours;
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        mBackgroundHandler = new Handler(thread.getLooper());
        mAnimationTask = new AnimationTask();
    }

    /**
     * @return true if an animation is currently active.
     * @see ca.rmen.nounours.Nounours#isAnimationRunning()
     */
    @Override
    public boolean isAnimationRunning() {
        Log.v(TAG, "isAnimationRunning");
        return mIsDoingAnimation.get();
    }

    /**
     * The user selected an animation from the menu. Display the animation.
     *
     * @see ca.rmen.nounours.Nounours#doAnimation(Animation, boolean)
     */
    @Override
    public void doAnimation(final Animation animation, final boolean isDynamicAnimation) {
        Log.v(TAG, "doAnimation: " + animation);
        mAnimationTask.setAnimation(animation, isDynamicAnimation);
        mBackgroundHandler.post(mAnimationTask);
    }

    /**
     * Stop the currently running animation, if there is one.
     *
     * @see ca.rmen.nounours.Nounours#stopAnimation()
     */
    @Override
    public void stopAnimation() {
        mIsDoingAnimation.set(false);
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

    private class AnimationTask implements Runnable {

        private Animation mAnimation;
        private boolean mIsDynamicAnimation;

        public void setAnimation(Animation animation, boolean isDynamicAnimation) {
            mAnimation = animation;
            mIsDynamicAnimation = isDynamicAnimation;
        }

        @Override
        public void run() {
            // Note that we are doing an animation
            mIsDoingAnimation.set(true);

            // Iterate through each of the images and display them.
            for (int i = 0; i < mAnimation.getRepeat(); i++) {
                for (AnimationImage image : mAnimation.getImages()) {
                    long before = System.currentTimeMillis();
                    mNounours.setImage(image.getImage());
                    long frameDuration = (long) (mAnimation.getInterval() * image.getDuration());
                    // after - before: time wasted displaying the image itself.
                    // we'll subtract it from the time to sleep for this frame.
                    long after = System.currentTimeMillis();
                    long frameDurationCorrection = after - before;
                    long shorterFrameDuration = frameDuration - frameDurationCorrection;
                    if (shorterFrameDuration > 0) SystemClock.sleep(shorterFrameDuration);
                    if(!mIsDoingAnimation.get()) break;
                }
                if(!mIsDoingAnimation.get()) break;
            }
            if (!mIsDynamicAnimation) mNounours.reset();
            // No longer doing an animation.
            mIsDoingAnimation.set(false);
        }
    }
}
