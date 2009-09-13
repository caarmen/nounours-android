/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import ca.rmen.nounours.data.Animation;
import ca.rmen.nounours.data.AnimationImage;
import ca.rmen.nounours.data.Image;
import ca.rmen.nounours.util.Trace;

/**
 * Manages the Nounours animations displayed to the Android device.
 *
 * @author Carmen Alvarez
 *
 */
public class AndroidNounoursAnimationHandler implements NounoursAnimationHandler {

    private AndroidNounours nounours = null;
    Activity activity = null;
    static Map<String, AnimationDrawable> animationCache = new HashMap<String, AnimationDrawable>();

    static Map<Bitmap,BitmapDrawable> bitmapDrawables = new HashMap<Bitmap,BitmapDrawable>();
    public AndroidNounoursAnimationHandler(AndroidNounours nounours, Activity activity) {
        this.nounours = nounours;
        this.activity = activity;
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
        final ImageView view = (ImageView) activity.findViewById(R.id.ImageView01);
        final Drawable currentDrawable = view.getDrawable();
        // First see if an animation is visible on the screen.
        if (currentDrawable instanceof AnimationDrawable) {
            return (AnimationDrawable) currentDrawable;
        }
        return null;
    }

    /**
     * The user selected an animation from the menu. Display the animation.
     *
     * @see ca.rmen.nounours.Nounours#doAnimation(java.lang.String)
     */
    public void doAnimation(final Animation animation) {
        final Runnable runnable = new Runnable() {
            public void run() {
                // Create an Android animation.
                final AnimationDrawable animationDrawable = createAnimation(animation);
                if (animationDrawable == null) {
                    Trace.debug(this, "No animation " + animation.getId());
                    return;
                }

                // Display the Android animation.
                final ImageView view = (ImageView) activity.findViewById(R.id.ImageView01);
                view.setImageDrawable(animationDrawable);

                animationDrawable.start();
                Trace.debug(this, "launched animation " + animation.getId());
            }
        };
        nounours.runTask(runnable);

    }

    /**
     * Stop the currently running animation, if there is one.
     *
     * @see ca.rmen.nounours.Nounours#stopAnimationImpl()
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
     * @param animation
     * @return
     */
    AnimationDrawable createAnimation(final Animation animation) {
        // First see if we have this stored in memory.
        AnimationDrawable animationDrawable = animationCache.get(animation.getId());
        if (animationDrawable != null) {
            return animationDrawable;
        }

        // Create the android animation.
        animationDrawable = new AnimationDrawable();

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

                BitmapDrawable drawable = getDrawable(bitmap);
                animationDrawable.addFrame(drawable, (int) (animation.getInterval() * animationImage.getDuration()));

            }
        }
        // Add the default image at the end.
        final Bitmap bitmap = nounours.getDrawableImage(nounours.getDefaultImage());
        BitmapDrawable drawable = getDrawable(bitmap);
        animationDrawable.addFrame(drawable, animation.getInterval());
        animationCache.put(animation.getId(), animationDrawable);
        Trace.debug(this, "Loaded animation " + animation.getId());

        return animationDrawable;
    }

	/**
	 * Store bitmap drawables for bitmaps in cache.
	 */
    private BitmapDrawable getDrawable(Bitmap bitmap)
    {
        BitmapDrawable result = bitmapDrawables.get(bitmap);
        if(result != null)
            return result;
        result = new BitmapDrawable(bitmap);
        bitmapDrawables.put(bitmap, result);
        return result;
    }

    /**
     * Store all animations in memory for performance.
     */
    void cacheAnimations() {
        final Map<String, Animation> animations = nounours.getAnimations();
        for (final String animationId : animations.keySet()) {
            final Animation animation = animations.get(animationId);
            createAnimation(animation);
        }
    }

    /**
     * The implementing class may implement this to add the menu item for the
     * animation, as it is read from the CSV file. If this must be handled
     * later, the method {#link {@link #getAnimations()} may be used instead.
     *
     * @param animation
     */
    public void addAnimation(Animation animation) {
        // Do nothing
    }

}