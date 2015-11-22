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

import java.util.Calendar;
import java.util.Locale;

import ca.rmen.nounours.android.common.nounours.NounoursRenderer;
import ca.rmen.nounours.android.common.settings.NounoursSettings;

/**
 * Renders nounours both in normal and ambient modes.
 */
class NounoursWatchFaceRenderer extends NounoursRenderer {

    private boolean mIsAmbient;
    private boolean mIsLowBitAmbient;
    private final Paint mBackgroundPaint;
    private final Bitmap mAmbientBitmap;

    public NounoursWatchFaceRenderer(Context context, NounoursSettings settings) {
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(settings.getBackgroundColor());
        int ambientBitmapId = context.getResources().getIdentifier("ambient_" + settings.getThemeId(), "drawable", context.getPackageName());
        BitmapDrawable ambientBitmapDrawable = (BitmapDrawable) context.getResources().getDrawable(ambientBitmapId, null);
        if (ambientBitmapDrawable != null) mAmbientBitmap = ambientBitmapDrawable.getBitmap();
        else mAmbientBitmap = null;
    }

    public void setIsAmbient(boolean isAmbient) {
        mIsAmbient = isAmbient;
    }

    public void setIsLowBitAmbient(boolean isLowBitAmbient) {
        mIsLowBitAmbient = isLowBitAmbient;
    }

    @Override
    public void render(NounoursSettings settings, Bitmap bitmap, Canvas canvas, int viewWidth, int viewHeight) {
        if (mIsAmbient) renderAmbientNounours(canvas, viewWidth, viewHeight);
        else super.render(settings, bitmap, canvas, viewWidth, viewHeight);
    }

    private void renderAmbientNounours(Canvas c, int viewWidth, int viewHeight) {
        c.drawRect(0, 0, viewWidth, viewHeight, mBackgroundPaint);
        if (mAmbientBitmap != null && !mIsLowBitAmbient) {
            // First figure out the largest possible square within the (possibly) rectangular view:
            // The square's upper left corner will have offsetX and offsetY
            // and the square will have a width squareViewWidth
            final int squareViewWidth;
            int offsetX = 0;
            int offsetY = 0;
            if (viewWidth < viewHeight) {
                squareViewWidth = viewWidth;
                offsetY = (viewHeight - viewWidth) / 2;
            } else {
                //noinspection SuspiciousNameCombination
                squareViewWidth = viewHeight;
                offsetX = (viewWidth - viewHeight) / 2;
            }

            // Draw nounours in a square which is 1/3 the square view width
            Rect nounoursDisplayViewRect = new Rect(squareViewWidth/ 3, squareViewWidth/ 3, 2 * squareViewWidth/ 3, 2 * squareViewWidth/ 3);

            // Rotate nounours according to the minutes of the current time.
            Calendar now = Calendar.getInstance(Locale.getDefault());
            float minutesRotation = 360 * now.get(Calendar.MINUTE) / 60;
            Matrix m = new Matrix();
            m.postRotate(minutesRotation, squareViewWidth/ 2, squareViewWidth/ 2);
            m.postTranslate(offsetX, offsetY);
            c.setMatrix(m);

            Rect bitmapRect = new Rect(0, 0, mAmbientBitmap.getWidth(), mAmbientBitmap.getHeight());
            c.drawBitmap(mAmbientBitmap, bitmapRect, nounoursDisplayViewRect, null);
            c.setMatrix(null);
        }
    }

}
