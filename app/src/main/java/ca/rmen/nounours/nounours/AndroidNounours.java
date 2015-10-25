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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import ca.rmen.nounours.Constants;
import ca.rmen.nounours.Nounours;
import ca.rmen.nounours.NounoursAnimationHandler;
import ca.rmen.nounours.NounoursSoundHandler;
import ca.rmen.nounours.NounoursVibrateHandler;
import ca.rmen.nounours.R;
import ca.rmen.nounours.data.Image;
import ca.rmen.nounours.data.Theme;
import ca.rmen.nounours.io.StreamLoader;
import ca.rmen.nounours.nounours.cache.ImageCache;
import ca.rmen.nounours.settings.NounoursSettings;
import ca.rmen.nounours.util.ThemeUtil;

/**
 * Implementation of the abstract Nounours class, containing logic specific to
 * Android.
 *
 * @author Carmen Alvarez
 */
public class AndroidNounours extends Nounours {

    public interface AndroidNounoursListener {
        void onThemeLoadStart(int max, String message);

        void onThemeLoadProgress(int progress, int max, String message);

        void onThemeLoadComplete();
    }

    private static final String TAG = Constants.TAG + AndroidNounours.class.getSimpleName();

    private final String mTag;
    private final Context mContext;
    private final Handler mUIHandler;
    private final NounoursSettings mSettings;
    private final SurfaceHolder mSurfaceHolder;
    private final AndroidNounoursListener mListener;
    private final ImageCache mImageCache = new ImageCache();
    private final SoundHandler mSoundHandler;
    private final Paint mPaint = new Paint();
    private int mBackgroundColor;
    private int mViewWidth;
    private int mViewHeight;
    private final AtomicBoolean mOkToDraw = new AtomicBoolean(false);

    /**
     * Open the CSV data files and call the superclass
     * {@link Nounours#init(StreamLoader, NounoursAnimationHandler, NounoursSoundHandler, NounoursVibrateHandler, InputStream, InputStream, String)}
     * method.
     *
     * @param tag     used for logging, to distinguish between the lwp and app instances
     * @param context The android mContext.
     */
    public AndroidNounours(String tag,
                           Context context,
                           Handler uiHandler,
                           NounoursSettings settings,
                           SurfaceHolder surfaceHolder,
                           AndroidNounoursListener listener) {

        mTag = "/" + tag;
        mContext = context;
        mUIHandler = uiHandler;
        mSettings = settings;
        mSurfaceHolder = surfaceHolder;
        mListener = listener;
        StreamLoader streamLoader = new AssetStreamLoader(context);

        String themeId = mSettings.getThemeId();
        AnimationHandler animationHandler = new AnimationHandler(this);
        mSoundHandler = new SoundHandler(context);
        VibrateHandler vibrateHandler = new VibrateHandler(context);
        final InputStream propertiesFile = context.getResources().openRawResource(R.raw.nounours);
        final InputStream themesFile = context.getResources().openRawResource(R.raw.themes);
        mSurfaceHolder.addCallback(mSurfaceHolderCallback);

        try {
            init(streamLoader, animationHandler, mSoundHandler, vibrateHandler, propertiesFile,
                    themesFile, themeId);
            setEnableVibrate(mSettings.isSoundEnabled());
            setEnableSound(mSettings.isSoundEnabled());
            setIdleTimeout(mSettings.getIdleTimeout());
        } catch (final IOException e) {
            Log.e(TAG + mTag, "Error initializing nounours", e);
        }
    }

    @Override
    protected boolean cacheResources() {
        boolean result = mImageCache.cacheImages(mContext, getCurrentTheme().getImages().values(), mUIHandler, mImageCacheListener);
        if (mSettings.isSoundEnabled()) mSoundHandler.cacheSounds(getCurrentTheme());
        return result;
    }

    /**
     * Load the new image set in a separate thread, showing the progress bar
     */
    @Override
    public void useTheme(final String id) {
        Log.v(TAG + mTag, "useTheme " + id);

        // Get the name of this theme.
        Theme theme = getThemes().get(id);
        CharSequence themeLabel = ThemeUtil.getThemeLabel(mContext, theme);

        // MEMORY
        mImageCache.clearImageCache();
        mSoundHandler.clearSoundCache();

        Thread themeLoader = new Thread() {
            @SuppressWarnings("synthetic-access")
            @Override
            public void run() {

                AndroidNounours.super.useTheme(id);

                runTask(new Runnable() {
                    public void run() {
                        mListener.onThemeLoadComplete();
                    }
                });
            }
        };
        mListener.onThemeLoadStart(theme.getImages().size(), mContext.getString(R.string.loading, themeLabel));
        themeLoader.start();
    }

    /**
     * Display a picture on the screen.
     *
     * @see ca.rmen.nounours.Nounours#displayImage(ca.rmen.nounours.data.Image)
     */
    @Override
    protected void displayImage(final Image image) {
        Log.v(TAG + mTag, "displayImage " + image);
        if (image == null) return;
        if (!mOkToDraw.get()) return;
        final Bitmap bitmap = mImageCache.getDrawableImage(mContext, image);
        if (bitmap == null) return;

        Canvas c = mSurfaceHolder.lockCanvas();
        if (c != null) {
            c.save();
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            int deviceCenterX = mViewWidth / 2;
            int deviceCenterY = mViewHeight / 2;
            int bitmapCenterX = bitmapWidth / 2;
            int bitmapCenterY = bitmapHeight / 2;

            float scaleX = (float) mViewWidth / bitmapWidth;
            float scaleY = (float) mViewHeight / bitmapHeight;
            float offsetX = deviceCenterX - bitmapCenterX;
            float offsetY = deviceCenterY - bitmapCenterY;

            float scaleToUse = (scaleX < scaleY) ? scaleX : scaleY;
            c.drawColor(mBackgroundColor);
            Matrix m = new Matrix();
            m.postTranslate(offsetX, offsetY);
            m.postScale(scaleToUse, scaleToUse, deviceCenterX, deviceCenterY);
            c.setMatrix(m);
            c.drawBitmap(bitmap, 0, 0, mPaint);
            c.restore();
            if (mSettings.isImageDimmed()) c.drawColor(0x88000000);
            mSurfaceHolder.unlockCanvasAndPost(c);
        }
    }

    public void redraw() {
        displayImage(getCurrentImage());
    }

    /**
     * Trace.
     */
    @Override
    protected void debug(final Object o) {
        if (o instanceof Throwable) {
            Throwable t = (Throwable) o;
            Log.w(TAG + mTag, t.getMessage(), t);
        } else {
            Log.v(TAG + mTag, "" + o);
        }
    }

    /**
     * UI threads should be run with an Android thread call.
     *
     * @see ca.rmen.nounours.Nounours#runTask(java.lang.Runnable)
     */
    @Override
    protected void runTask(final Runnable task) {
        mUIHandler.post(task);
    }

    /**
     * Cleanup.
     */
    public void onDestroy() {
        Log.v(TAG + mTag, "destroy");
        mImageCache.clearImageCache();
        mSoundHandler.clearSoundCache();
    }

    @Override
    protected int getDeviceHeight() {
        return mViewHeight;
    }

    @Override
    protected int getDeviceWidth() {
        return mViewWidth;
    }

    /**
     * Reread the shared preferences and apply the new app_settings.
     */
    public void reloadSettings() {
        if (mSettings.isSoundEnabled() && !isSoundEnabled()) {
            mSoundHandler.cacheSounds(getCurrentTheme());
        } else if (!mSettings.isSoundEnabled() && isSoundEnabled()) {
            mSoundHandler.clearSoundCache();
        }

        setEnableSound(mSettings.isSoundEnabled());
        setEnableVibrate(mSettings.isSoundEnabled());
        setIdleTimeout(mSettings.getIdleTimeout());
        mBackgroundColor = mSettings.getBackgroundColor();
        reloadThemeFromPreference();
    }

    private void reloadThemeFromPreference() {
        Log.v(TAG + mTag, "reloadThemeFromPreference");
        boolean nounoursIsBusy = isLoading();
        Log.v(TAG + mTag, "reloadThemeFromPreference, nounoursIsBusy = " + nounoursIsBusy);
        String themeId = mSettings.getThemeId();
        if (getCurrentTheme() != null && getCurrentTheme().getId().equals(themeId)) {
            return;
        }
        final Theme theme = getThemes().get(themeId);
        if (theme != null) {
            stopAnimation();
            useTheme(theme.getId());
        }
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            Log.v(TAG + mTag, "surfaceCreated");
            mOkToDraw.set(true);
            redraw();
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
            Log.v(TAG + mTag, "surfaceChanged");
            mViewWidth = width;
            mViewHeight = height;
            redraw();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            Log.v(TAG + mTag, "surfaceDestroyed");
            mOkToDraw.set(false);
        }
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final ImageCache.ImageCacheListener mImageCacheListener = new ImageCache.ImageCacheListener() {
        @Override
        public void onImageLoaded(final Image image, int progress, int total) {
            Log.v(TAG + mTag, "onImageLoaded: " + progress + "/" + total);
            setImage(image);
            CharSequence themeName = ThemeUtil.getThemeLabel(mContext, getCurrentTheme());
            mListener.onThemeLoadProgress(progress, total, mContext.getString(R.string.loading, themeName));
        }
    };
}
