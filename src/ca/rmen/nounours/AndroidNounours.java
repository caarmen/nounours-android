/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
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
    ProgressDialog progressDialog;

    static Map<String, Bitmap> imageCache = new HashMap<String, Bitmap>();

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

        /*
         * Runnable task = new Runnable() {
         *
         * @Override public void run() {
         */
        AndroidNounoursAnimationHandler animationHandler = new AndroidNounoursAnimationHandler(AndroidNounours.this,
                activity);
        AndroidNounoursSoundHandler soundHandler = new AndroidNounoursSoundHandler(AndroidNounours.this, activity);
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

        // Cache animations.
        animationHandler.cacheAnimations();
        /*
         * }
         *
         * }; runTaskWithProgressBar(task, true);
         */
    }

    /**
     * Load the images into memory.
     */
    protected void cacheImages() {
        for (Image image : getImages().values()) {
            loadImage(image);
        }
    }

    /**
     * Load the new image set in a separate thread, showing the progress bar
     */
    @Override
    public void useImageSet(final String id) {
        Runnable imageCacher = new Runnable() {
            @SuppressWarnings("synthetic-access")
            @Override
            public void run() {

                AndroidNounours.super.useImageSet(id);
            }
        };
        runTaskWithProgressBar(imageCacher, false);

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
        final Bitmap bitmap = getDrawableImage(image);
        final ImageView imageView = (ImageView) activity.findViewById(R.id.ImageView01);
        imageView.setImageBitmap(bitmap);
    }

    /**
     * Find the Android image for the given nounours image.
     *
     * @param image
     * @return
     */
    Bitmap getDrawableImage(final Image image) {
        Bitmap res = imageCache.get(image.getId());
        if (res == null) {
            debug("Loading drawable image " + image);
            res = loadImage(image);
        }
        return res;
    }

    /**
     * Load an image from the disk into memory. Return the Drawable for the
     * iamge.
     *
     * @param image
     * @return
     */
    Bitmap loadImage(final Image image) {
        debug("Loading " + image + " into memory");
        Bitmap cachedBitmap = imageCache.get(image.getId());
        Bitmap newBitmap = null;
        String themesDir = getProperty(PROP_DOWNLOADED_IMAGES_DIR);
        // This is one of the default images bundled in the apk.
        if (image.getFilename().contains(themesDir)) {
            // Load the new image
            debug("Load themed image.");
            newBitmap = BitmapFactory.decodeFile(image.getFilename());
        }
        // This is one of the downloaded images, in the sdcard.
        else {
            final int imageResId = activity.getResources().getIdentifier(image.getFilename(), "drawable",
                    activity.getClass().getPackage().getName());
            // Load the image from the resource file.
            debug("Load default image " + imageResId);
            Bitmap readOnlyBitmap = ((BitmapDrawable)activity.getResources().getDrawable(imageResId)).getBitmap();
            // Store the newly loaded drawable in cache for the first time.
            if (cachedBitmap == null) {
                // Make a mutable copy of the drawable.
                Bitmap bitmapCopy = readOnlyBitmap.copy(readOnlyBitmap.getConfig(), true);
                Canvas canvas = new Canvas(bitmapCopy);
                canvas.drawBitmap(readOnlyBitmap, 0, 0, null);
                readOnlyBitmap.recycle();
                imageCache.put(image.getId(), bitmapCopy);
                return bitmapCopy;
            }
            newBitmap = readOnlyBitmap;
        }
        // We already cached a Drawable. Replace its contents.
        // Draw the new image into the cached drawable
        Canvas canvas = new Canvas(cachedBitmap);
        canvas.drawBitmap(newBitmap, 0, 0, null);

        // Get rid of the temporary image.
        newBitmap.recycle();
        return cachedBitmap;
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

    /**
     * Run a task, showing the progress bar while the task runs.
     *
     * @param task
     * @param ui
     *            if true, use the android api to run the task. Otherwise use
     *            the standard java thread api.
     */
    protected void runTaskWithProgressBar(final Runnable task, boolean ui) {
        if (progressDialog != null)
            progressDialog.dismiss();
        progressDialog = ProgressDialog.show(activity, "", activity.getString(R.string.loading), true);
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                task.run();
                progressDialog.dismiss();
            }
        };
        if (ui)
            runTask(runnable);
        else
            new Thread(runnable).start();
    }
}
