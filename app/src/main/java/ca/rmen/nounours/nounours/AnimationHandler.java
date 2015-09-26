/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours.nounours;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

import ca.rmen.nounours.NounoursAnimationHandler;
import ca.rmen.nounours.data.Animation;
import ca.rmen.nounours.data.AnimationImage;
import ca.rmen.nounours.data.Image;
import ca.rmen.nounours.compat.BitmapCompat;
import ca.rmen.nounours.util.Trace;

/**
 * Manages the Nounours animations displayed to the Android device.
 *
 * @author Carmen Alvarez
 */
class AnimationHandler implements NounoursAnimationHandler {

    private AndroidNounours nounours = null;
    final private ImageView imageView;
    private static final Map<String, AnimationDrawable> animationCache = new HashMap<>();

    private static final Map<Bitmap, BitmapDrawable> bitmapDrawables = new HashMap<>();

    public AnimationHandler(AndroidNounours nounours, ImageView imageView) {
        this.nounours = nounours;
        this.imageView = imageView;
    }

    public void reset() {
        for (AnimationDrawable animationDrawable : animationCache.values()) {
            purgeAnimationDrawable(animationDrawable);
        }
        animationCache.clear();
        for (Bitmap bitmap : bitmapDrawables.keySet()) {
            if (bitmap != null && !bitmap.isRecycled())
                bitmap.recycle();
        }
        bitmapDrawables.clear();
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
        Trace.debug(this, "isAnimationRunning");
        final AnimationDrawable currentAnimation = getCurrentAnimationDrawable();
        if (currentAnimation == null) {
            Trace.debug(this, "Not running any animation");
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
            Trace.debug(this, "isRunning true, yet on last image?");
            currentAnimation.stop();
            return false;
        }
        Trace.debug(this, "Currently running animation");
        return true;
    }

    /**
     * @return the currently running android animation, if any.
     */
    private AnimationDrawable getCurrentAnimationDrawable() {
        final Drawable currentDrawable = imageView.getDrawable();
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
        final Runnable runnable = new Runnable() {
            public void run() {
                // Create an Android animation.
                final AnimationDrawable animationDrawable = createAnimation(animation, !isDynamicAnimation);
                if (animationDrawable == null) {
                    Trace.debug(this, "No animation " + animation.getId());
                    return;
                }

                // Display the Android animation.
                imageView.setImageDrawable(animationDrawable);

                animationDrawable.start();
                Trace.debug(this, "launched animation " + animation.getId());
            }
        };
        nounours.runTask(runnable);

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
        Trace.debug(this, "createAnimation " + animation + " doCache = " + doCache);
        // First see if we have this stored in memory.
        AnimationDrawable animationDrawable = animationCache.get(animation.getId());
        if (animationDrawable != null) {
            return animationDrawable;
        }

        // Create the android animation.
        animationDrawable = new AnimationDrawable();
        if (doCache)
            animationCache.put(animation.getId(), animationDrawable);

        // Go through the list of images in the nounours animation, "repeat"
        // times.
        for (int i = 0; i < animation.getRepeat(); i++) {
            for (final AnimationImage animationImage : animation.getImages()) {
                // Make sure the image exists.
                final Image image = nounours.getImages().get(animationImage.getImageId());
                if (image == null) {
                    Trace.debug(this, "No image " + animationImage);
                    return null;
                }
                // Get the android image and add it to the android animation.
                final Bitmap bitmap = nounours.getDrawableImage(image);
                if (bitmap == null)
                    return null;

                BitmapDrawable drawable = getDrawable(bitmap);
                if (drawable == null)
                    return null;
                animationDrawable.addFrame(drawable, (int) (animation.getInterval() * animationImage.getDuration()));

            }
        }
        // Add the default image at the end.
        final Bitmap bitmap = nounours.getDrawableImage(nounours.getDefaultImage());
        if (bitmap == null)
            return null;
        BitmapDrawable drawable = getDrawable(bitmap);
        if (drawable == null)
            return null;
        animationDrawable.addFrame(drawable, animation.getInterval());
        Trace.debug(this, "Loaded animation " + animation.getId());

        return animationDrawable;
    }

    /**
     * Store bitmap drawables for bitmaps in cache.
     */
    private BitmapDrawable getDrawable(Bitmap bitmap) {
        BitmapDrawable result = bitmapDrawables.get(bitmap);
        if (result != null)
            return result;
        result = BitmapCompat.createBitmapDrawable(nounours.context, bitmap);
        bitmapDrawables.put(bitmap, result);
        return result;
    }

    /**
     * Store all animations in memory for performance.
     */
    boolean cacheAnimations() {
        Trace.debug(this, "cacheAnimations");
        final Map<String, Animation> animations = nounours.getAnimations();
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
        animationCache.clear();
        bitmapDrawables.clear();
    }
}
