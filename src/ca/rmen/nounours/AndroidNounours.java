/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;
import ca.rmen.nounours.data.Image;
import ca.rmen.nounours.util.Trace;

/**
 * Implementation of the abstract Nounours class, containing logic specific to
 * Android.
 *
 * @author Carmen Alvarez
 *
 */
public class AndroidNounours extends Nounours {

    NounoursActivity activity = null;

    static Map<String, Drawable> imageCache = new HashMap<String, Drawable>();

    /**
     * Open the CSV data files and call the superclass
     * {@link #init(InputStream, InputStream, InputStream, InputStream, InputStream, String)}
     * method.
     *
     * @param activity
     *            The android activity.
     */
    public AndroidNounours(final NounoursActivity activity) {
        this.activity = activity;
        AndroidNounoursAnimationHandler animationHandler = new AndroidNounoursAnimationHandler(this, activity);
        AndroidNounoursSoundHandler soundHandler = new AndroidNounoursSoundHandler(this, activity);
        AndroidNounoursVibrateHandler vibrateHandler = new AndroidNounoursVibrateHandler(activity);
        final InputStream propertiesFile = activity.getResources().openRawResource(R.raw.nounours);
        final InputStream imageFile = activity.getResources().openRawResource(R.raw.image);
        final InputStream imageSetFile = activity.getResources().openRawResource(R.raw.imageset);
        final InputStream featureFile = activity.getResources().openRawResource(R.raw.feature);
        final InputStream imageFeatureFile = activity.getResources().openRawResource(R.raw.imagefeatureassoc);
        final InputStream adjacentImageFile = activity.getResources().openRawResource(R.raw.adjacentimage);
        final InputStream animationFile = activity.getResources().openRawResource(R.raw.animation);
        final InputStream flingAnimationFile = activity.getResources().openRawResource(R.raw.flinganimation);
        final InputStream soundFile = activity.getResources().openRawResource(R.raw.sound);

        try {
            init(animationHandler, soundHandler, vibrateHandler, propertiesFile, imageFile, imageSetFile, featureFile,
                    imageFeatureFile, adjacentImageFile, animationFile, flingAnimationFile, soundFile, "Default"); //$NON-NLS-1$
        } catch (final IOException e) {
            Log.d(getClass().getName(), "Error initializing nounours", e); //$NON-NLS-1$
        }

        cacheImages();
        // Cache animations.
        animationHandler.cacheAnimations();
    }

    /**
     * Load the images into memory.
     */
    private void cacheImages()
    {
        for(Image image: getImages().values())
        {
            getDrawableImage(image);
        }
    }
    /**
     * Display a picture on the screen.
     *
     * @see ca.rmen.nounours.Nounours#displayImage(ca.rmen.nounours.Image)
     */
    @Override
    protected void displayImage(final Image image) {
        if (image == null) {
            return;
        }
        final Drawable drawable = getDrawableImage(image);
        final ImageView imageView = (ImageView) activity.findViewById(R.id.ImageView01);
        imageView.setImageDrawable(drawable);
    }

    /**
     * Change the theme.
     */
    @Override
    public void useImageSet(String id) {
        super.useImageSet(id);

        for (Image image : getImages().values()) {
            debug("Loading " + image + " into memory");
            // Load the new image
            BitmapDrawable newDrawable = (BitmapDrawable) Drawable.createFromPath(image.getFilename());
            BitmapDrawable oldDrawable = (BitmapDrawable) imageCache.get(image.getId());
            // Error checking
            if (oldDrawable == null) {
                debug("No cached image found for " + image);
                continue;
            }
            if (newDrawable == null) {
                debug("Did not create image from " + image.getFilename());
                continue;
            }
            // Draw the new image into the cached drawable
            Canvas canvas = new Canvas(oldDrawable.getBitmap());
            canvas.drawBitmap(newDrawable.getBitmap(), 0, 0, null);

            // Get rid of the temporary image.
            newDrawable.getBitmap().recycle();
            System.gc();
        }

    }

    /**
     * Find the Android image for the given nounours image.
     *
     * @param image
     * @return
     */
    Drawable getDrawableImage(final Image image) {
        Drawable res = imageCache.get(image.getId());
        if (res == null) {
            debug("Loading drawable image " + image);
            final int imageResId = activity.getResources().getIdentifier(image.getFilename(), "drawable",
                    activity.getClass().getPackage().getName());
            // Load the image from the resource file.
            BitmapDrawable drawable = (BitmapDrawable) activity.getResources().getDrawable(imageResId);
            Bitmap bitmap = drawable.getBitmap();
            // Make a mutable copy of the drawable.
            Bitmap bitmapCopy = bitmap.copy(bitmap.getConfig(), true);
            Canvas canvas = new Canvas(bitmapCopy);
            drawable.draw(canvas);
            res = new BitmapDrawable(bitmapCopy);
            drawable.getBitmap().recycle();

            imageCache.put(image.getId(), res);
        }
        return res;
    }

    /**
     * Trace.
     *
     */
    @Override
    protected void debug(final Object o) {
        Trace.debug(this, o);
    }

    /**
     * UI threads should be run with an Android thread call.
     *
     * @see ca.rmen.nounours.Nounours#runTask(java.lang.Runnable)
     */
    @Override
    protected void runTask(final Runnable task) {
        final ImageView imageView = (ImageView) activity.findViewById(R.id.ImageView01);
        imageView.post(task);
    }
}
