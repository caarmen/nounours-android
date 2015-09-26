/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import ca.rmen.nounours.data.Image;
import ca.rmen.nounours.data.Theme;
import ca.rmen.nounours.util.DisplayCompat;
import ca.rmen.nounours.util.FileUtil;
import ca.rmen.nounours.util.Trace;

/**
 * Implementation of the abstract Nounours class, containing logic specific to
 * Android.
 *
 * @author Carmen Alvarez
 */
class AndroidNounours extends Nounours {

    Context context = null;
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    private static final String PREF_THEME = "Theme";
    private static final String PREF_THEME_UPDATE = "ThemeUpdate";
    static final String PREF_SOUND_AND_VIBRATE = "SoundAndVibrate";
    static final String PREF_RANDOM = "Random";
    static final String PREF_IDLE_TIMEOUT = "IdleTimeout";
    private SharedPreferences sharedPreferences = null;
    private AndroidNounoursAnimationHandler animationHandler = null;
    final private ImageView imageView;

    private final ImageCache imageCache;


    /**
     * Open the CSV data files and call the superclass
     * {@link Nounours#init(NounoursAnimationHandler, NounoursSoundHandler, NounoursVibrateHandler, InputStream, InputStream, InputStream, InputStream, InputStream, InputStream, InputStream, InputStream, InputStream, InputStream, String)}
     * method.
     *
     * @param context The android context.
     */
    public AndroidNounours(final Context context, ImageView imageView) {

        this.context = context;
        this.imageView = imageView;
        imageCache = new ImageCache(context, imageCacheListener);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String themeId = sharedPreferences.getString(PREF_THEME, Nounours.DEFAULT_THEME_ID);
        if (!FileUtil.isSdPresent())
            themeId = Nounours.DEFAULT_THEME_ID;
        animationHandler = new AndroidNounoursAnimationHandler(this, imageView);
        AndroidNounoursSoundHandler soundHandler = new AndroidNounoursSoundHandler(this, context);
        AndroidNounoursVibrateHandler vibrateHandler = new AndroidNounoursVibrateHandler(context);
        final InputStream propertiesFile = context.getResources().openRawResource(R.raw.nounours);
        final InputStream themePropertiesFile = context.getResources().openRawResource(R.raw.nounoursdeftheme);

        final InputStream imageFile = context.getResources().openRawResource(R.raw.image);
        final InputStream imageSetFile = context.getResources().openRawResource(R.raw.imageset);
        final InputStream featureFile = context.getResources().openRawResource(R.raw.feature);
        final InputStream imageFeatureFile = context.getResources().openRawResource(R.raw.imagefeatureassoc);
        final InputStream adjacentImageFile = context.getResources().openRawResource(R.raw.adjacentimage);
        final InputStream animationFile = context.getResources().openRawResource(R.raw.animation);
        final InputStream flingAnimationFile = context.getResources().openRawResource(R.raw.flinganimation);
        final InputStream soundFile = context.getResources().openRawResource(R.raw.sound);

        try {
            init(animationHandler, soundHandler, vibrateHandler, propertiesFile, themePropertiesFile, imageFile,
                    imageSetFile, featureFile, imageFeatureFile, adjacentImageFile, animationFile, flingAnimationFile,
                    soundFile, themeId);
            setEnableVibrate(AndroidNounoursSettings.isSoundEnabled(context));
            setEnableSound(AndroidNounoursSettings.isSoundEnabled(context));
            setEnableRandomAnimations(AndroidNounoursSettings.isRandomAnimationEnabled(context));
            setIdleTimeout(AndroidNounoursSettings.getIdleTimeout(context));
        } catch (final IOException e) {
            Log.d(getClass().getName(), "Error initializing nounours", e); //$NON-NLS-1$
        }
    }

    @Override
    protected boolean cacheImages() {
        if(!imageCache.cacheImages(getImages().values())) return false;
        // Cache animations.
        return animationHandler.cacheAnimations();
    }

    /**
     * Load the new image set in a separate thread, showing the progress bar
     */
    @Override
    public boolean useTheme(final String id) {
        if (!Nounours.DEFAULT_THEME_ID.equals(id)) {
            File themeDir = new File(getAppDir(), id);
            if (!themeDir.exists()) {
                if(themeDir.mkdirs()) {
                    Trace.debug(this, "Could not create theme folder " + themeDir);
                }
            }
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
        imageCache.clearImageCache();
        animationHandler.reset();
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
                    CharSequence message = context.getText(R.string.themeLoadError);

                    showAlertDialog(message, revertToDefaultTheme);
                }

                runTask(new Runnable() {
                    public void run() {
                        resizeView();
                    }
                });

            }
        };
        runTaskWithProgressBar(imageCacher, context.getString(R.string.predownload, getThemeLabel(theme)),
                taskSize);
        return true;

    }

    private void resizeView() {
        Theme theme = getCurrentTheme();
        if (theme == null)
            return;
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();

        float widthRatio = (float) DisplayCompat.getWidth(context) / theme.getWidth();
        float heightRatio = (float) DisplayCompat.getHeight(context) / theme.getHeight();
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
        updateProgressBar(progress, 2 * max, context.getString(R.string.downloading, themeLabel));
    }

    protected void updatePreloadProgress(int progress, int max) {
        CharSequence themeLabel = getThemeLabel(getCurrentTheme());
        updateProgressBar(progress, 2 * max, context.getString(R.string.predownload, themeLabel));
    }

    CharSequence getThemeLabel(Theme theme) {
        String themeLabel = theme.getName();
        int themeLabelId = context.getResources().getIdentifier(theme.getName(), "string",
                getClass().getPackage().getName());
        if (themeLabelId > 0)
            return context.getResources().getText(themeLabelId);
        return themeLabel;
    }

    /**
     * Display a picture on the screen.
     *
     * @see ca.rmen.nounours.Nounours#displayImage(ca.rmen.nounours.data.Image)
     */
    @Override
    protected void displayImage(final Image image) {
        if (image == null) {
            return;
        }
        final Bitmap bitmap = imageCache.getDrawableImage(image);
        if (bitmap == null)
            return;
        imageView.setImageBitmap(bitmap);
    }

    /**
     * Find the Android image for the given nounours image.
     */
    Bitmap getDrawableImage(final Image image) {
        return imageCache.getDrawableImage(image);
    }

    /**
     * Trace.
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
        imageView.post(task);
    }

    /**
     * Run a task, showing the progress bar while the task runs.
     */
    private void runTaskWithProgressBar(final Runnable task, String message, int max) {
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
        new Thread(runnable).start();
    }

    /**
     * Update the currently showing progress bar.
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
                progressDialog.setMax(max);
                progressDialog.setMessage(message);
                debug("updateProgressBar " + progress + "/" + max + ": " + message);

            }
        };
        runTask(runnable);
    }

    /**
     * Create a determinate progress dialog with the given size and text.
     */
    private void createProgressDialog(int max, String message) {
        progressDialog = new ProgressDialog(context);
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
        imageCache.clearImageCache();
        animationHandler.onDestroy();
    }

    @Override
    protected int getDeviceHeight() {
        return imageView.getHeight();
    }

    @Override
    protected int getDeviceWidth() {
        return imageView.getWidth();
    }

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

    private void showAlertDialog(final CharSequence message, final OnClickListener callback) {
        Runnable showAlert = new Runnable() {
            public void run() {
                if (alertDialog == null) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);

                    alertBuilder.setMessage(context.getText(R.string.themeLoadError));
                    alertBuilder.setPositiveButton(context.getText(android.R.string.ok), callback);

                    alertDialog = alertBuilder.create();

                }
                alertDialog.setMessage(message);
                alertDialog.show();
            }
        };
        runTask(showAlert);
    }

    @Override
    public File getAppDir() {
        return FileUtil.getSdFolder(context);
    }

    private final ImageCache.ImageCacheListener imageCacheListener = new ImageCache.ImageCacheListener() {
        @Override
        public void onImageLoaded(final Image image, int progress, int total) {
            Runnable runnable = new Runnable() {
                public void run() {
                    setImage(image);
                }
            };
            runTask(runnable);
            CharSequence themeName = getThemeLabel(context, getCurrentTheme());
            updateProgressBar(total + (progress), 2 * total, context.getString(R.string.loading, themeName));

        }
    };
}
