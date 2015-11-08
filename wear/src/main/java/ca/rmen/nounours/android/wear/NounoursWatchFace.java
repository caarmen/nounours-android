/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.rmen.nounours.android.wear;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import ca.rmen.nounours.android.common.Constants;
import ca.rmen.nounours.android.common.compat.ResourcesCompat;
import ca.rmen.nounours.android.common.nounours.AndroidNounours;
import ca.rmen.nounours.android.common.nounours.EmptySoundHandler;
import ca.rmen.nounours.android.common.nounours.EmptyVibrateHandler;
import ca.rmen.nounours.android.common.nounours.NounoursRenderer;
import ca.rmen.nounours.android.common.settings.NounoursSettings;
import ca.rmen.nounours.data.Image;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class NounoursWatchFace extends CanvasWatchFaceService {
    private static final String TAG = Constants.TAG + NounoursWatchFace.class.getSimpleName();
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        final Handler mUpdateTimeHandler = new EngineHandler(this);

        private Paint mBackgroundPaint;
        private Paint mTextPaint;

        private boolean mAmbient;

        private Calendar mTime;

        private float mXOffset;
        private float mYOffset;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        private boolean mLowBitAmbient;
        private AndroidNounours mNounours;
        private NounoursSettings mSettings;
        private WearNounoursResourceCache mCache;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            Log.v(TAG, "onCreate");

            Context context = getApplicationContext();
            setWatchFaceStyle(new WatchFaceStyle.Builder(NounoursWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)
                    .build());
            Resources resources = NounoursWatchFace.this.getResources();
            mYOffset = resources.getDimension(R.dimen.digital_y_offset);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(ResourcesCompat.getColor(context, R.color.digital_background));

            mTextPaint = new Paint();
            mTextPaint.setColor(ResourcesCompat.getColor(context, R.color.digital_text));
            mTextPaint.setTypeface(NORMAL_TYPEFACE);
            mTextPaint.setAntiAlias(true);

            mTime = Calendar.getInstance(Locale.getDefault());
            mSettings = new WearSettings();
            mSettings.setEnableSound(false);
            mCache = new WearNounoursResourceCache(getApplicationContext());
            mNounours = new AndroidNounours("WEAR",
                    getApplicationContext(),
                    new Handler(),
                    mSettings,
                    getSurfaceHolder(),
                    mRenderer,
                    mCache,
                    new EmptySoundHandler(),
                    new EmptyVibrateHandler(),
                    mListener);
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            mNounours.onDestroy();
            super.onDestroy();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                // Update time zone in case it changed while we weren't visible.
                mNounours.doPing(true);
            } else {
                mNounours.doPing(false);
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            super.onTapCommand(tapType, x, y, eventTime);
            if (tapType == WatchFaceService.TAP_TYPE_TAP) {
                mNounours.doRandomAnimation();
            }
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = NounoursWatchFace.this.getResources();
            boolean isRound = insets.isRound();
            mXOffset = resources.getDimension(isRound
                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            float textSize = resources.getDimension(isRound
                    ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);

            mTextPaint.setTextSize(textSize);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                mNounours.doPing(!mAmbient);
                if (mLowBitAmbient) {
                    mTextPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
            if (!mNounours.isLoading()) {
                Image image = mNounours.getCurrentImage();
                if (image != null) {
                    Bitmap bitmap = mCache.getDrawableImage(getApplicationContext(), image);
                    renderNounours(canvas, bitmap, bounds.width(), bounds.height());
                }
                renderTime(canvas);
            }
        }

        void renderNounours(Canvas c, Bitmap bitmap, int viewWidth, int viewHeight) {
            c.drawColor(mSettings.getBackgroundColor());
            if (mAmbient) {
                c.drawRect(0, 0, viewWidth, viewHeight, mBackgroundPaint);
            } else {
                int bitmapWidth = bitmap.getWidth();
                int bitmapHeight = bitmap.getHeight();
                int deviceCenterX = viewWidth / 2;
                int deviceCenterY = viewHeight / 2;
                int bitmapCenterX = bitmapWidth / 2;
                int bitmapCenterY = bitmapHeight / 2;

                float scaleX = (float) viewWidth / bitmapWidth;
                float scaleY = (float) viewHeight / bitmapHeight;
                float offsetX = deviceCenterX - bitmapCenterX;
                float offsetY = deviceCenterY - bitmapCenterY;

                float scaleToUse = (scaleX < scaleY) ? scaleX : scaleY;
                Matrix m = new Matrix();
                m.postTranslate(offsetX, offsetY);
                m.postScale(scaleToUse, scaleToUse, deviceCenterX, deviceCenterY);
                c.setMatrix(m);
                c.drawBitmap(bitmap, 0, 0, mBackgroundPaint);
                if (mSettings.isImageDimmed()) c.drawColor(0x88000000);
            }
        }

        void renderTime(Canvas c) {
            mTime.setTime(new Date(System.currentTimeMillis()));

            // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
            String text = mAmbient
                    ? String.format("%d:%02d",
                    mTime.get(Calendar.HOUR),
                    mTime.get(Calendar.MINUTE))
                    : String.format("%d:%02d:%02d",
                    mTime.get(Calendar.HOUR),
                    mTime.get(Calendar.MINUTE),
                    mTime.get(Calendar.SECOND));
            c.drawText(text, mXOffset, mYOffset, mTextPaint);
        }

        private NounoursRenderer mRenderer = new NounoursRenderer() {
            @Override
            public void render(NounoursSettings settings, Bitmap bitmap,
                               SurfaceHolder surfaceHolder, int viewWidth, int viewHeight,
                               int backgroundColor, Paint paint) {
                Canvas c = surfaceHolder.lockCanvas();
                if (c != null) {
                    renderNounours(c, bitmap, viewWidth, viewHeight);
                    renderTime(c);
                    surfaceHolder.unlockCanvasAndPost(c);
                }
            }
        };

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

        private final AndroidNounours.AndroidNounoursListener mListener = new AndroidNounours.AndroidNounoursListener() {
            @Override
            public void onThemeLoadStart(int max, String message) {
            }

            @Override
            public void onThemeLoadProgress(int progress, int max, String message) {
            }

            @Override
            public void onThemeLoadComplete() {
            }
        };
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<NounoursWatchFace.Engine> mWeakReference;

        public EngineHandler(NounoursWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            NounoursWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }
}
