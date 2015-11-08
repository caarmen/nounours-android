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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.view.SurfaceHolder;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ca.rmen.nounours.R;
import ca.rmen.nounours.android.common.compat.ResourcesCompat;
import ca.rmen.nounours.android.common.nounours.NounoursRenderer;
import ca.rmen.nounours.android.common.settings.NounoursSettings;

class NounoursWatchFaceRenderer extends NounoursRenderer {
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    private boolean mIsAmbient;
    private boolean mIsLowBitAmbient;
    private final Calendar mCalendar;
    private final Paint mBackgroundPaint;
    private final Paint mTextPaint;
    private final Bitmap mAmbientBitmap;
    private float mXOffset;
    private float mYOffset;

    public NounoursWatchFaceRenderer(Context context, NounoursSettings settings) {
        mCalendar = Calendar.getInstance(Locale.getDefault());
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(ResourcesCompat.getColor(context, settings.getBackgroundColor()));
        mTextPaint = new Paint();
        mTextPaint.setColor(ResourcesCompat.getColor(context, R.color.digital_text));
        mTextPaint.setTypeface(NORMAL_TYPEFACE);
        mTextPaint.setAntiAlias(true);
        int ambientBitmapId = context.getResources().getIdentifier("ambient_" + settings.getThemeId(), "drawable", context.getPackageName());
        mAmbientBitmap = ((BitmapDrawable) context.getResources().getDrawable(ambientBitmapId, null)).getBitmap();
    }

    public void setIsAmbient(boolean isAmbient) {
        mIsAmbient = isAmbient;
        if (mIsLowBitAmbient) {
            mTextPaint.setAntiAlias(!isAmbient);
        }
    }

    public void setIsLowBitAmbient(boolean isLowBitAmbient) {
        mIsLowBitAmbient = isLowBitAmbient;
    }

    public void setTextSize(float textSize) {
        mTextPaint.setTextSize(textSize);
    }

    public void setOffset(float xOffset, float yOffset) {
        mXOffset = xOffset;
        mYOffset = yOffset;
    }

    @Override
    public void render(NounoursSettings settings, Bitmap bitmap, SurfaceHolder surfaceHolder, int viewWidth, int viewHeight, int backgroundColor, Paint paint) {
        Canvas c = surfaceHolder.lockCanvas();
        if (c != null) {
            renderNounours(settings, c, bitmap, viewWidth, viewHeight);
            renderTime(c);
            surfaceHolder.unlockCanvasAndPost(c);
        }
    }

    void renderNounours(NounoursSettings settings, Canvas canvas, Bitmap bitmap, int viewWidth, int viewHeight) {
        if (mIsAmbient) renderAmbientNounours(canvas, viewWidth, viewHeight);
        else renderNormalNounours(settings, canvas, bitmap, viewWidth, viewHeight);
    }

    private void renderNormalNounours(NounoursSettings settings, Canvas c, Bitmap bitmap, int viewWidth, int viewHeight) {
        c.drawRect(0, 0, viewWidth, viewHeight, mBackgroundPaint);
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
        if (settings.isImageDimmed()) c.drawColor(0x88000000);
    }

    private void renderAmbientNounours(Canvas c, int viewWidth, int viewHeight) {
        c.drawRect(0, 0, viewWidth, viewHeight, mBackgroundPaint);
        if (!mIsLowBitAmbient) {
            Rect bitmapRect = new Rect(0, 0, mAmbientBitmap.getWidth(), mAmbientBitmap.getHeight());
            Rect viewRect = new Rect(viewWidth / 3, viewHeight / 3, 2 * viewWidth / 3, 2 * viewHeight / 3);
            float minutesRotation = 360 * mCalendar.get(Calendar.MINUTE) / 60;
            Matrix m = new Matrix();
            m.postRotate(minutesRotation, viewWidth / 2, viewHeight / 2);
            c.setMatrix(m);
            c.drawBitmap(mAmbientBitmap, bitmapRect, viewRect, null);
            c.setMatrix(new Matrix());
        }
    }

    void renderTime(Canvas c) {

        mCalendar.setTime(new Date(System.currentTimeMillis()));
        // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
        String text = mIsAmbient
                ? String.format("%d:%02d",
                mCalendar.get(Calendar.HOUR),
                mCalendar.get(Calendar.MINUTE))
                : String.format("%d:%02d:%02d",
                mCalendar.get(Calendar.HOUR),
                mCalendar.get(Calendar.MINUTE),
                mCalendar.get(Calendar.SECOND));
        c.drawText(text, mXOffset, mYOffset, mTextPaint);
    }

}
