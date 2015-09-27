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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import ca.rmen.nounours.Nounours;
import ca.rmen.nounours.NounoursAnimationHandler;
import ca.rmen.nounours.NounoursSoundHandler;
import ca.rmen.nounours.NounoursVibrateHandler;
import ca.rmen.nounours.R;
import ca.rmen.nounours.compat.DisplayCompat;
import ca.rmen.nounours.compat.EnvironmentCompat;
import ca.rmen.nounours.data.Image;
import ca.rmen.nounours.data.Theme;
import ca.rmen.nounours.settings.NounoursSettings;
import ca.rmen.nounours.util.FileUtil;
import ca.rmen.nounours.util.ThemeUtil;
import ca.rmen.nounours.util.Trace;

/**
 * Implementation of the abstract Nounours class, containing logic specific to
 * Android.
 *
 * @author Carmen Alvarez
 */
public class AndroidNounours extends Nounours {

    Context context = null;
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    private AnimationHandler animationHandler = null;
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

        String themeId = NounoursSettings.getThemeId(context);
        if (!FileUtil.isSdPresent())
            themeId = Nounours.DEFAULT_THEME_ID;
        animationHandler = new AnimationHandler(this, imageView);
        SoundHandler soundHandler = new SoundHandler(this, context);
        VibrateHandler vibrateHandler = new VibrateHandler(context);
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
            setEnableVibrate(NounoursSettings.isSoundEnabled(context));
            setEnableSound(NounoursSettings.isSoundEnabled(context));
            setEnableRandomAnimations(NounoursSettings.isRandomAnimationEnabled(context));
            setIdleTimeout(NounoursSettings.getIdleTimeout(context));
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
     * Something went wrong when trying to load a theme.  Reset to the default one.
     */
    private void resetToDefaultTheme() {
        Trace.debug(this, "resetToDefaultTheme");
        OnClickListener revertToDefaultTheme = new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                NounoursSettings.setThemeId(context, DEFAULT_THEME_ID);
                useTheme(Nounours.DEFAULT_THEME_ID);
            }
        };
        CharSequence message = context.getText(R.string.themeLoadError);

        showAlertDialog(message, revertToDefaultTheme);
    }

    /**
     * Load the new image set in a separate thread, showing the progress bar
     */
    @Override
    public boolean useTheme(final String id) {
        if (!Nounours.DEFAULT_THEME_ID.equals(id)) {
            File themeDir = new File(getAppDir(), id);
            if (!themeDir.exists()) {
                boolean mkdirsResult = themeDir.mkdirs();
                if(!themeDir.isDirectory()) {
                    Trace.debug(this, "Could not create theme folder " + themeDir + ". mkdirs returned " + mkdirsResult);
                    resetToDefaultTheme();
                    return false;
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

        // MEMORY
        imageCache.clearImageCache();
        animationHandler.reset();
        Runnable imageCacher = new Runnable() {
            @SuppressWarnings("synthetic-access")
            @Override
            public void run() {

                boolean loadedTheme = AndroidNounours.super.useTheme(id);
                if (!loadedTheme) {
                    if (!Nounours.DEFAULT_THEME_ID.equals(id)) {
                        debug("Could not load theme " + id + ":  load default theme instead");
                        resetToDefaultTheme();
                    }
                }

                runTask(new Runnable() {
                    public void run() {
                        resizeView();
                    }
                });

            }
        };
        runTaskWithProgressBar(imageCacher, context.getString(R.string.predownload, ThemeUtil.getThemeLabel(context, theme)),
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
        CharSequence themeLabel = ThemeUtil.getThemeLabel(context, getCurrentTheme());
        updateProgressBar(progress, 2 * max, context.getString(R.string.downloading, themeLabel));
    }

    protected void updatePreloadProgress(int progress, int max) {
        CharSequence themeLabel = ThemeUtil.getThemeLabel(context, getCurrentTheme());
        updateProgressBar(progress, 2 * max, context.getString(R.string.predownload, themeLabel));
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
        return EnvironmentCompat.getExternalFilesDir(context);
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final ImageCache.ImageCacheListener imageCacheListener = new ImageCache.ImageCacheListener() {
        @Override
        public void onImageLoaded(final Image image, int progress, int total) {
            Runnable runnable = new Runnable() {
                public void run() {
                    setImage(image);
                }
            };
            runTask(runnable);
            CharSequence themeName = ThemeUtil.getThemeLabel(context, getCurrentTheme());
            updateProgressBar(total + (progress), 2 * total, context.getString(R.string.loading, themeName));

        }
    };
}
