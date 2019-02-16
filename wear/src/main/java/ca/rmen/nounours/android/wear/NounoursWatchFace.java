/*
 *   Copyright (c) 2015 Carmen Alvarez
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

package ca.rmen.nounours.android.wear;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import ca.rmen.nounours.R;
import ca.rmen.nounours.android.common.Constants;
import ca.rmen.nounours.android.common.compat.ResourcesCompat;
import ca.rmen.nounours.android.common.nounours.AndroidNounours;
import ca.rmen.nounours.android.common.nounours.EmptySoundHandler;
import ca.rmen.nounours.android.common.nounours.EmptyThemeLoadListener;
import ca.rmen.nounours.android.common.nounours.EmptyVibrateHandler;
import ca.rmen.nounours.android.common.nounours.cache.ImageCache;
import ca.rmen.nounours.android.common.nounours.cache.NounoursResourceCache;
import ca.rmen.nounours.data.Image;

/**
 * Low-bit ambient mode: displays the time using the system format.
 * Ambient mode (not low-bit): displays the time using the system format, and a gray scale image of
 * Nounours rotated according to the time.
 * Normal mode: displays the time using the system format, and a color image of Nounours. In
 * normal mode, Nounours is somewhat interactive: a simple tap will trigger an animation. This is
 * less interaction than the handheld app and live wallpaper versions.
 */
public abstract class NounoursWatchFace extends CanvasWatchFaceService {
    private static final String TAG = Constants.TAG + NounoursWatchFace.class.getSimpleName();

    protected abstract WearSettings getSettings();

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        private boolean mIsAmbient;
        private AndroidNounours mNounours;
        private WearSettings mSettings;
        private NounoursResourceCache mCache;
        private NounoursWatchFaceRenderer mRenderer;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            Log.v(TAG, "onCreate");

            mSettings = getSettings();
            setWatchFaceStyle();

            Context context = getApplicationContext();

            mSettings.setBackgroundColor(ResourcesCompat.getColor(getApplicationContext(), R.color.background_color));
            mRenderer = new NounoursWatchFaceRenderer(context, mSettings);
            mCache = new NounoursResourceCache(context, mSettings, new ImageCache());
            PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(mSharedPrefsListener);
            mNounours = new AndroidNounours("WEAR",
                    getApplicationContext(),
                    new Handler(),
                    mSettings,
                    getSurfaceHolder(),
                    mRenderer,
                    mCache,
                    new EmptySoundHandler(),
                    new EmptyVibrateHandler(),
                    new EmptyThemeLoadListener());
        }

        private void setWatchFaceStyle() {
            setWatchFaceStyle(new WatchFaceStyle.Builder(NounoursWatchFace.this)
                    .setShowSystemUiTime(mSettings.isDigitalTimeEnabled() || !mIsAmbient)
                    .setAcceptsTapEvents(true)
                    .build());
        }

        @Override
        public void onDestroy() {
            mNounours.onDestroy();
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).unregisterOnSharedPreferenceChangeListener(mSharedPrefsListener);
            super.onDestroy();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            Log.v(TAG, "onVisibilityChanged: visible = " + visible + ", ambient = " + mIsAmbient);
            mNounours.doPing(visible && !mIsAmbient);
        }

        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            super.onTapCommand(tapType, x, y, eventTime);
            if (mNounours.isLoading()) return;
            if (tapType == WatchFaceService.TAP_TYPE_TAP) {
                mNounours.doRandomAnimation();
            }
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mRenderer.setIsLowBitAmbient(properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT));
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            Log.v(TAG, "onAmbientModeChanged: ambient = " + inAmbientMode);
            mRenderer.setIsAmbient(inAmbientMode);
            mNounours.doPing(!inAmbientMode);
            if (mIsAmbient != inAmbientMode) {
                mIsAmbient = inAmbientMode;
                setWatchFaceStyle();
                invalidate();
            }
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            mRenderer.setIsRound(insets.isRound());
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            if (!mNounours.isLoading()) {
                Image image = mNounours.getCurrentImage();
                if (image != null) {
                    Bitmap bitmap = mCache.getDrawableImage(getApplicationContext(), image);
                    mRenderer.render(mSettings, bitmap, canvas, bounds.width(), bounds.height());
                }
            }
        }
        private final SharedPreferences.OnSharedPreferenceChangeListener mSharedPrefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                setWatchFaceStyle();
            }
        };
    }

}
