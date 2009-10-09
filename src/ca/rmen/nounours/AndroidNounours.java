/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import ca.rmen.nounours.data.Image;
import ca.rmen.nounours.util.FileUtil;
import ca.rmen.nounours.util.PhoneHome;
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
    private static final String PREF_THEME = "Theme";
    private SharedPreferences sharedPreferences = null;
    private AndroidNounoursAnimationHandler animationHandler = null;

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

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String themeId = sharedPreferences.getString(PREF_THEME, Nounours.DEFAULT_THEME_ID);
        if (!FileUtil.isSdPresent())
            themeId = Nounours.DEFAULT_THEME_ID;
        PhoneHome.phoneHome(activity, sharedPreferences, themeId, "");

        /*
         * Runnable task = new Runnable() {
         * 
         * @Override public void run() {
         */
        animationHandler = new AndroidNounoursAnimationHandler(AndroidNounours.this, activity);
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
                    imageFeatureFile, adjacentImageFile, animationFile, flingAnimationFile, soundFile,
                    "Default", themeId); //$NON-NLS-1$
        } catch (final IOException e) {
            Log.d(getClass().getName(), "Error initializing nounours", e); //$NON-NLS-1$
        }

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

        int i = 0;
        int max = getImages().size();
        for (final Image image : getImages().values()) {
            loadImage(image, 10);
            Runnable runnable = new Runnable() {
                public void run() {
                    setImage(image);
                }
            };
            runTask(runnable);
            updateProgressBar(max + (i++), 2 * max, activity.getString(R.string.loading));
        }
        // Cache animations.
        animationHandler.cacheAnimations();

    }

    /**
     * Load the new image set in a separate thread, showing the progress bar
     */
    @Override
    public void useImageSet(final String id) {
        Editor editor = sharedPreferences.edit();
        editor.putString(PREF_THEME, id);
        editor.commit();
        Runnable imageCacher = new Runnable() {
            @SuppressWarnings("synthetic-access")
            @Override
            public void run() {

                AndroidNounours.super.useImageSet(id);
            }
        };
        runTaskWithProgressBar(imageCacher, false, activity.getString(R.string.predownload), 2 * getImages().size());

    }

    /**
     * Update the progress bar with the download status.
     */
    @Override
    protected void updateDownloadProgress(int progress, int max) {
        updateProgressBar(progress, 2 * max, activity.getString(R.string.downloading));
    }

    protected void updatePreloadProgress(int progress, int max) {
        updateProgressBar(progress, 2 * max, activity.getString(R.string.predownload));
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
            res = loadImage(image, 10);
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
    Bitmap loadImage(final Image image, int retries) {
        debug("Loading " + image + " into memory");
        Bitmap cachedBitmap = imageCache.get(image.getId());
        Bitmap newBitmap = null;
        String themesDir = getProperty(PROP_DOWNLOADED_IMAGES_DIR);
        try {
            boolean useDefaultImage = true;
            // This is one of the downloaded images, in the sdcard.
            if (image.getFilename().contains(themesDir)) {
                // Load the new image
                debug("Load themed image.");
                newBitmap = BitmapFactory.decodeFile(image.getFilename());
                // If the image is corrupt or missing, use the default image.
                if (newBitmap == null) {
                    File imageFile = new File(image.getFilename());
                    imageFile.delete();
                    String defaultImageFileName = imageFile.getName();
                    image.setFilename(defaultImageFileName);
                }
                // We have a valid theme image.
                else
                    useDefaultImage = false;
            }
            // This is one of the default images bundled in the apk.
            if (useDefaultImage) {
                final int imageResId = activity.getResources().getIdentifier(image.getFilename(), "drawable",
                        activity.getClass().getPackage().getName());
                // Load the image from the resource file.
                debug("Load default image " + imageResId);
                Bitmap readOnlyBitmap = BitmapFactory.decodeResource(activity.getResources(), imageResId);// ((BitmapDrawable)
                // activity.getResources().getDrawable(imageResId)).getBitmap();
                debug("default image mutable = " + readOnlyBitmap.isMutable() + ", recycled="
                        + readOnlyBitmap.isRecycled());
                // Store the newly loaded drawable in cache for the first time.
                if (cachedBitmap == null) {
                    // Make a mutable copy of the drawable.
                    cachedBitmap = copyAndCacheImage(readOnlyBitmap, image.getId());
                    return cachedBitmap;
                }
                newBitmap = readOnlyBitmap;
            }
            if (cachedBitmap == null) {
                debug("Image not in cache");
            } else if (cachedBitmap.isRecycled()) {
                debug("Cached image was recycled!");
            }

            // No cached bitmap, using a theme. This will happen if the user
            // loads
            // the app up with a non-default theme.
            if (cachedBitmap == null) {
                cachedBitmap = copyAndCacheImage(newBitmap, image.getId());
                return cachedBitmap;
            }
            // We already cached a Drawable. Replace its contents.
            // Draw the new image into the cached drawable
            Canvas canvas = new Canvas(cachedBitmap);
            canvas.drawBitmap(newBitmap, 0, 0, null);

            // Get rid of the temporary image.
            if (newBitmap != null)
                newBitmap.recycle();
            return cachedBitmap;
        } catch (OutOfMemoryError error) {
            debug("Memory error loading " + image + ". " + retries + " retries left");
            if (retries > 0) {
                System.gc();
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return loadImage(image, retries - 1);
            }
            return null;
        }
    }

    /**
     * Create a mutable copy of the given immutable bitmap, and store it in the
     * cache.
     * 
     * @param readOnlyBitmap
     *            the immutable bitmap
     * @param imageId
     * @return
     */
    private Bitmap copyAndCacheImage(Bitmap readOnlyBitmap, String imageId) {
        Bitmap mutableBitmap = readOnlyBitmap.copy(readOnlyBitmap.getConfig(), true);
        Canvas canvas = new Canvas(mutableBitmap);
        canvas.drawBitmap(readOnlyBitmap, 0, 0, null);
        readOnlyBitmap.recycle();
        imageCache.put(imageId, mutableBitmap);
        return mutableBitmap;
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
    protected void runTaskWithProgressBar(final Runnable task, boolean ui, String message, int max) {
        if (progressDialog != null)
            progressDialog.dismiss();
        createProgressDialog(max, message);
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

    /**
     * Update the currently showing progress bar.
     * 
     * @param progress
     * @param max
     * @param message
     */
    private void updateProgressBar(final int progress, final int max, final String message) {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                // show the progress bar if it is not already showing.
                if (progressDialog == null || !progressDialog.isShowing())
                    createProgressDialog(max, message);
                // Update the progress
                progressDialog.setProgress(progress);
                progressDialog.setMessage(message);
                debug("updateProgressBar " + progress + "/" + max + ": " + message);

            }
        };
        runTask(runnable);
    }

    /**
     * Create a determinate progress dialog with the given size and text.
     * 
     * @param max
     * @param message
     */
    void createProgressDialog(int max, String message) {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle("");
        progressDialog.setMessage(message);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(max);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.setCancelable(false);
        progressDialog.show();
        debug("createProgressDialog " + max + ": " + message);
    }

    /**
     * Cleanup.
     */
    public void onDestroy() {
        debug("destroy");
        for (String imageId : imageCache.keySet()) {
            Bitmap bitmap = imageCache.get(imageId);
            if (!bitmap.isRecycled())
                bitmap.recycle();
        }
        imageCache.clear();
        animationHandler.onDestroy();
    }
}
