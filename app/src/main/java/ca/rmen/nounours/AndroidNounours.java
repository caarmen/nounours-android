/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Environment;
import android.preference.PreferenceManager;
//import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.ImageView;
import ca.rmen.nounours.data.Image;
import ca.rmen.nounours.data.Theme;
import ca.rmen.nounours.util.FileUtil;
import ca.rmen.nounours.util.GoogleAnalyticsHelper;
import ca.rmen.nounours.util.Trace;

/**
 * Implementation of the abstract Nounours class, containing logic specific to
 * Android.
 * 
 * @author Carmen Alvarez
 * 
 */
public class AndroidNounours extends Nounours {

	//private static final double MIN_SIZE_INCHES_FOR_HD = 5.0;
	
    NounoursActivity activity = null;
    ProgressDialog progressDialog;
    AlertDialog alertDialog;
    private static final String PREF_THEME = "Theme";
    private static final String PREF_THEME_UPDATE = "ThemeUpdate";
    static final String PREF_SOUND_AND_VIBRATE = "SoundAndVibrate";
    static final String PREF_RANDOM = "Random";
    static final String PREF_IDLE_TIMEOUT = "IdleTimeout";
    private static final String ANALYTICS_TRACKER_ID = "UA-XXXXXX-1";
    private SharedPreferences sharedPreferences = null;
    private AndroidNounoursAnimationHandler animationHandler = null;

    static Map<String, Bitmap> imageCache = new HashMap<String, Bitmap>();
    //private boolean useHd = false;

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
        GoogleAnalyticsHelper.track(activity, sharedPreferences, false, ANALYTICS_TRACKER_ID, themeId, null);
        boolean enableSoundAndVibrate = sharedPreferences.getBoolean(PREF_SOUND_AND_VIBRATE, true);
        boolean enableRandomAnimations = sharedPreferences.getBoolean(PREF_RANDOM, true);
        long idleTimeout = sharedPreferences.getLong(PREF_IDLE_TIMEOUT, 30000);
        /*
         * Runnable task = new Runnable() {
         * 
         * @Override public void run() {
         */
        animationHandler = new AndroidNounoursAnimationHandler(AndroidNounours.this, activity);
        AndroidNounoursSoundHandler soundHandler = new AndroidNounoursSoundHandler(AndroidNounours.this, activity);
        AndroidNounoursVibrateHandler vibrateHandler = new AndroidNounoursVibrateHandler(activity);
        final InputStream propertiesFile = activity.getResources().openRawResource(R.raw.nounours);
        final InputStream themePropertiesFile = activity.getResources().openRawResource(R.raw.nounoursdeftheme);

        final InputStream imageFile = activity.getResources().openRawResource(R.raw.image);
        final InputStream imageSetFile = activity.getResources().openRawResource(R.raw.imageset);
        final InputStream featureFile = activity.getResources().openRawResource(R.raw.feature);
        final InputStream imageFeatureFile = activity.getResources().openRawResource(R.raw.imagefeatureassoc);
        final InputStream adjacentImageFile = activity.getResources().openRawResource(R.raw.adjacentimage);
        final InputStream animationFile = activity.getResources().openRawResource(R.raw.animation);
        final InputStream flingAnimationFile = activity.getResources().openRawResource(R.raw.flinganimation);
        final InputStream soundFile = activity.getResources().openRawResource(R.raw.sound);

        try {
            init(animationHandler, soundHandler, vibrateHandler, propertiesFile, themePropertiesFile, imageFile,
                    imageSetFile, featureFile, imageFeatureFile, adjacentImageFile, animationFile, flingAnimationFile,
                    soundFile, themeId);
            setEnableVibrate(enableSoundAndVibrate);
            setEnableSound(enableSoundAndVibrate);
            setEnableRandomAnimations(enableRandomAnimations);
            setIdleTimeout(idleTimeout);
        } catch (final IOException e) {
            Log.d(getClass().getName(), "Error initializing nounours", e); //$NON-NLS-1$
        }
        /*
    	DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        
        double x = dm.widthPixels/dm.xdpi;
        double y = dm.heightPixels/dm.ydpi;
        double screenInches = Math.sqrt(x*x+y*y);
        useHd = screenInches >= MIN_SIZE_INCHES_FOR_HD;
        Log.d("hello", "size inches: " + x + "x" + y + ":" + screenInches);
        Log.d("hello", "dpi: " + dm.xdpi + "x" + dm.ydpi);
        Log.d("hello", "size pixels: " + dm.widthPixels + "x" + dm.heightPixels);
        */

    }

    /**
     * Load the images into memory.
     */
    protected boolean cacheImages() {

        CharSequence themeName = getThemeLabel(getCurrentTheme());
        int i = 0;
        int max = getImages().size();
        for (final Image image : getImages().values()) {
            Bitmap bitmap = loadImage(image, 10);
            if (bitmap == null)
                return false;
            Runnable runnable = new Runnable() {
                public void run() {
                    setImage(image);
                }
            };
            runTask(runnable);
            updateProgressBar(max + (i++), 2 * max, activity.getString(R.string.loading, themeName));
        }
        // Cache animations.
        return animationHandler.cacheAnimations();

    }

    /**
     * Load the new image set in a separate thread, showing the progress bar
     */
    @Override
    public boolean useTheme(final String id) {
        if(!Nounours.DEFAULT_THEME_ID.equals(id))
        {
            File themeDir = new File(getAppDir(), id);
            if(!themeDir.exists())
                themeDir.mkdirs();
        }
        int taskSize = 1;
        Theme theme = getThemes().get(id);
        if (theme == null || theme.getImages() == null || theme.getImages().size() == 0)
            theme = getCurrentTheme();
        if (theme == null || theme.getImages() == null || theme.getImages().size() == 0)
            theme = getDefaultTheme();
        if (theme != null && theme.getImages() != null && theme.getImages().size() > 0 && theme.getSounds().size() > 0)
            taskSize = theme.getImages().size() * 2 + theme.getSounds().size();

        Editor editor = sharedPreferences.edit();
        editor.putString(PREF_THEME, id);
        editor.commit();
        // MEMORY
        clearImageache();
        Runnable imageCacher = new Runnable() {
            @SuppressWarnings("synthetic-access")
            @Override
            public void run() {

                boolean loadedTheme = AndroidNounours.super.useTheme(id);
                if (!loadedTheme) {
                    debug("Could not load theme " + id + ":  load default theme instead");
                    OnClickListener revertToDefaultTheme = new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!Nounours.DEFAULT_THEME_ID.equals(id))
                                useTheme(Nounours.DEFAULT_THEME_ID);

                        }
                    };
                    CharSequence message = activity.getText(R.string.themeLoadError);

                    showAlertDialog(message, revertToDefaultTheme);
                }

                runTask(new Runnable() {
                    public void run() {
                        resizeView();
                    }
                });

            }
        };
        runTaskWithProgressBar(imageCacher, false, activity.getString(R.string.predownload, getThemeLabel(theme)),
                taskSize);
        return true;

    }

    private void resizeView() {
        Theme theme = getCurrentTheme();
        if (theme == null)
            return;
        final ImageView imageView = (ImageView) activity.findViewById(R.id.ImageView01);
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        Display display = activity.getWindowManager().getDefaultDisplay();

        float widthRatio = (float) display.getWidth() / theme.getWidth();
        float heightRatio = (float) display.getHeight() / theme.getHeight();
        Trace.debug(this, widthRatio + ": " + heightRatio);
        float ratioToUse = widthRatio > heightRatio ? heightRatio : widthRatio;

        layoutParams.height = (int) (ratioToUse * theme.getHeight());
        layoutParams.width = (int) (ratioToUse * theme.getWidth());
        Trace.debug(this, "Scaling view to " + layoutParams.width + "x" + layoutParams.height);
        imageView.setLayoutParams(layoutParams);

    }

    /**
     * Update the progress bar with the download status.
     */
    @Override
    protected void updateDownloadProgress(int progress, int max) {
        CharSequence themeLabel = getThemeLabel(getCurrentTheme());
        updateProgressBar(progress, 2 * max, activity.getString(R.string.downloading, themeLabel));
    }

    protected void updatePreloadProgress(int progress, int max) {
        CharSequence themeLabel = getThemeLabel(getCurrentTheme());
        updateProgressBar(progress, 2 * max, activity.getString(R.string.predownload, themeLabel));
    }

    CharSequence getCurrentThemeLabel() {
        Theme curTheme = getCurrentTheme();
        if (curTheme != null)
            return getThemeLabel(curTheme);
        return activity.getResources().getText(R.string.defaultTheme);
    }

    CharSequence getThemeLabel(Theme theme) {
        String themeLabel = theme.getName();
        int themeLabelId = activity.getResources().getIdentifier(theme.getName(), "string",
                getClass().getPackage().getName());
        if (themeLabelId > 0)
            return activity.getResources().getText(themeLabelId);
        return themeLabel;
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
        if (bitmap == null)
            return;
        final ImageView imageView = (ImageView) activity.findViewById(R.id.ImageView01);
        imageView.setImageBitmap(bitmap);
    }

    private void clearImageache() {

        for (Bitmap bitmap : imageCache.values()) {
            if (!bitmap.isRecycled())
                bitmap.recycle();
        }
        imageCache.clear();
        animationHandler.reset();
        System.gc();

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
        try {
            // This is one of the downloaded images, in the sdcard.
            if (image.getFilename().contains(getAppDir().getAbsolutePath())) {
                // Load the new image
                debug("Load themed image.");
                newBitmap = BitmapFactory.decodeFile(image.getFilename());
                // If the image is corrupt or missing, use the default image.
                if (newBitmap == null) {
                    return null;
                }
            }
            // This is one of the default images bundled in the apk.
            else {
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
            } else
                cachedBitmap.recycle();

            // No cached bitmap, using a theme. This will happen if the user
            // loads
            // the app up with a non-default theme.
            cachedBitmap = copyAndCacheImage(newBitmap, image.getId());
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
    void updateProgressBar(final int progress, final int max, final String message) {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                // show the progress bar if it is not already showing.
                if (progressDialog == null || !progressDialog.isShowing())
                    createProgressDialog(max, message);
                // Update the progress
                progressDialog.setProgress(progress);
                progressDialog.setMax(max);
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
        progressDialog.setIndeterminate(max < 0);
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

    @Override
    protected int getDeviceHeight() {
        final ImageView imageView = (ImageView) activity.findViewById(R.id.ImageView01);
        return imageView.getHeight();
    }

    @Override
    protected int getDeviceWidth() {
        final ImageView imageView = (ImageView) activity.findViewById(R.id.ImageView01);
        return imageView.getWidth();
    }
    /*
    @Override
    protected boolean useHd(){
    	return useHd;
    }*/

    @Override
    protected boolean isThemeUpToDate(Theme theme) {
        return true;
    }

    @Override
    protected void setIsThemeUpToDate(Theme theme) {
        Editor editorTS = sharedPreferences.edit();
        String prefKey = PREF_THEME_UPDATE + theme.getId();
        editorTS.putLong(prefKey, new Date().getTime());
        editorTS.commit();

    }

    @Override
    public void setIdleTimeout(long idleTimeout) {
        super.setIdleTimeout(idleTimeout);
        Editor editor = sharedPreferences.edit();
        editor.putLong(PREF_IDLE_TIMEOUT, idleTimeout);
        editor.commit();
    }

    void showAlertDialog(final CharSequence message, final OnClickListener callback) {
        Runnable showAlert = new Runnable() {
            public void run() {
                if (alertDialog == null) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);

                    alertBuilder.setMessage(activity.getText(R.string.themeLoadError));
                    alertBuilder.setPositiveButton(activity.getText(R.string.ok), callback);

                    alertDialog = alertBuilder.create();

                }
                alertDialog.setMessage(message);
                alertDialog.show();
            }
        };
        runTask(showAlert);
    }
    
    @Override
    public File getAppDir()
    {
        File sdcard = Environment.getExternalStorageDirectory();
        if (sdcard != null && sdcard.exists()) {
            String appDirName = getProperty(Nounours.PROP_DOWNLOADED_IMAGES_DIR);
            File appDir = new File(sdcard, appDirName);
            if (!appDir.exists())
                appDir.mkdirs();
            return appDir;
        }
        return null;
        

    }
}
