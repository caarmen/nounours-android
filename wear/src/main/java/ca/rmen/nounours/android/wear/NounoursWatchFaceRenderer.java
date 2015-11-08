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
import android.graphics.drawable.BitmapDrawable;
import android.view.SurfaceHolder;

import java.util.Calendar;
import java.util.Locale;

import ca.rmen.nounours.android.common.compat.ResourcesCompat;
import ca.rmen.nounours.android.common.nounours.NounoursRenderer;
import ca.rmen.nounours.android.common.settings.NounoursSettings;

class NounoursWatchFaceRenderer extends NounoursRenderer {

    private boolean mIsAmbient;
    private boolean mIsLowBitAmbient;
    private final Paint mBackgroundPaint;
    private final Bitmap mAmbientBitmap;

    public NounoursWatchFaceRenderer(Context context, NounoursSettings settings) {
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(ResourcesCompat.getColor(context, settings.getBackgroundColor()));
        int ambientBitmapId = context.getResources().getIdentifier("ambient_" + settings.getThemeId(), "drawable", context.getPackageName());
        mAmbientBitmap = ((BitmapDrawable) context.getResources().getDrawable(ambientBitmapId, null)).getBitmap();
    }

    public void setIsAmbient(boolean isAmbient) {
        mIsAmbient = isAmbient;
    }

    public void setIsLowBitAmbient(boolean isLowBitAmbient) {
        mIsLowBitAmbient = isLowBitAmbient;
    }

    @Override
    public void render(NounoursSettings settings, Bitmap bitmap, SurfaceHolder surfaceHolder, int viewWidth, int viewHeight, int backgroundColor, Paint paint) {
        Canvas c = surfaceHolder.lockCanvas();
        if (c != null) {
            renderNounours(settings, c, bitmap, viewWidth, viewHeight);
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
            // Draw nounours in a square which is 1/3 the device width, and 1/3 the device height.
            Rect viewRect = new Rect(viewWidth / 3, viewHeight / 3, 2 * viewWidth / 3, 2 * viewHeight / 3);
            Calendar now = Calendar.getInstance(Locale.getDefault());
            float minutesRotation = 360 * now.get(Calendar.MINUTE) / 60;
            Matrix m = new Matrix();
            m.postRotate(minutesRotation, viewWidth / 2, viewHeight / 2);
            c.setMatrix(m);
            c.drawBitmap(mAmbientBitmap, bitmapRect, viewRect, null);
            c.setMatrix(null);
        }
    }

}
