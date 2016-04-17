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
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.VisibleForTesting;

import java.util.Calendar;
import java.util.Locale;

import ca.rmen.nounours.R;
import ca.rmen.nounours.android.common.compat.ResourcesCompat;
import ca.rmen.nounours.android.common.nounours.NounoursRenderer;
import ca.rmen.nounours.android.common.settings.NounoursSettings;

/**
 * Renders nounours both in normal and ambient modes.
 */
class NounoursWatchFaceRenderer extends NounoursRenderer {

    private boolean mIsRound;
    private boolean mIsAmbient;
    private boolean mIsLowBitAmbient;
    private final Paint mBackgroundPaint;
    private final Bitmap mAmbientBitmap;
    private final Bitmap mLowBitAmbientBitmap;
    private final int mDialNumberColor;
    private final int mDialNumberTextSize;

    public NounoursWatchFaceRenderer(Context context, NounoursSettings settings) {
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(settings.getBackgroundColor());
        mAmbientBitmap = getBitmap(context, "ambient_" + settings.getThemeId());
        mLowBitAmbientBitmap = getBitmap(context, "low_bit_ambient_" + settings.getThemeId());
        mDialNumberColor = ResourcesCompat.getColor(context, android.R.color.white);
        mDialNumberTextSize = context.getResources().getDimensionPixelSize(R.dimen.dial_number_text_size);
    }

    private Bitmap getBitmap(Context context, String identifier) {
        int bitmapId = context.getResources().getIdentifier(identifier, "drawable", context.getPackageName());
        if (bitmapId == 0) return null;
        BitmapDrawable bitmapDrawable = (BitmapDrawable) context.getResources().getDrawable(bitmapId, null);
        if (bitmapDrawable != null) return bitmapDrawable.getBitmap();
        return null;
    }

    public void setIsRound(boolean isRound) {
        mIsRound = isRound;
    }

    public void setIsAmbient(boolean isAmbient) {
        mIsAmbient = isAmbient;
    }

    public void setIsLowBitAmbient(boolean isLowBitAmbient) {
        mIsLowBitAmbient = isLowBitAmbient;
    }

    @Override
    public void render(NounoursSettings settings, Bitmap bitmap, Canvas canvas, int viewWidth, int viewHeight) {
        if (mIsAmbient) renderAmbientNounours((WearSettings) settings, canvas, viewWidth, viewHeight);
        else super.render(settings, bitmap, canvas, viewWidth, viewHeight);
    }

    private void renderAmbientNounours(WearSettings settings, Canvas c, int viewWidth, int viewHeight) {
        c.drawRect(0, 0, viewWidth, viewHeight, mBackgroundPaint);
        Bitmap bitmap = mIsLowBitAmbient ? mLowBitAmbientBitmap : mAmbientBitmap;
        if (bitmap != null) {
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

            // Rotate nounours around himself according to the minutes of the current time.
            Calendar now = Calendar.getInstance(Locale.getDefault());
            float minutesRotation = 360 * now.get(Calendar.MINUTE) / 60;

            // Place nounours somewhere around the edge of the watch, according to the hour of the current time.
            // timeInHours: ex: 8:30am and 8:30pm would both be 0.708333
            float timeInHours = now.get(Calendar.HOUR) + (float) now.get(Calendar.MINUTE)/60;
            float hoursRotation = 90 - (360 * timeInHours / 12);
            float offsetHoursX = (float) Math.cos(Math.toRadians(hoursRotation)) * squareViewWidth / 3;
            float offsetHoursY = -(float) Math.sin(Math.toRadians(hoursRotation)) * squareViewWidth / 3;
            Matrix m = new Matrix();
            m.postRotate(minutesRotation, squareViewWidth/ 2, squareViewWidth/ 2);
            m.postTranslate(offsetX, offsetY);
            m.postTranslate(offsetHoursX, offsetHoursY);
            c.setMatrix(m);

            Rect bitmapRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            c.drawBitmap(bitmap, bitmapRect, nounoursDisplayViewRect, null);
            c.setMatrix(null);
            if (!settings.isDigitalTimeEnabled()) {
                renderDialNumbers(c, viewWidth, viewHeight);
            }
        }
    }

    private void renderDialNumbers(Canvas c, int viewWidth, int viewHeight) {
        Rect textBounds = new Rect();
        Paint paint = new Paint();
        paint.setTextSize(mDialNumberTextSize);
        for (int dialNumber = 1; dialNumber <= 12; dialNumber++) {
            String dialNumberLabel = String.valueOf(dialNumber);
            paint.getTextBounds(dialNumberLabel, 0, dialNumberLabel.length(), textBounds);
            float altTextWidth = paint.measureText(dialNumberLabel);
            int textHeight = textBounds.height();
            int textWidth = (int) Math.max(textBounds.width(), altTextWidth);
            Point dialNumberPosition = mIsRound ?
                    getDialNumberPositionInCircle(dialNumber, viewWidth, textWidth, textHeight) :
                    getDialNumberPositionInRect(dialNumber, viewWidth, viewHeight, textWidth, textHeight);
            paint.setColor(mDialNumberColor);
            c.drawText(dialNumberLabel,
                    dialNumberPosition.x - textWidth / 2,
                    dialNumberPosition.y + textHeight / 2,
                    paint);
        }
    }

    /**
     * @param dialNumber   from 1 to 12.
     * @param numberWidth  the width of the image of the dial number
     * @param numberHeight the height of the image of the dial number
     * @return the position of the center of the dial number image, relative to the upper-left corner of the rectangular screen.
     */
    @VisibleForTesting
    static Point getDialNumberPositionInRect(int dialNumber, double screenWidth, double screenHeight, double numberWidth, double numberHeight) {
        double degrees = 90 - (dialNumber * 30);
        Point outerRimPoint = getOuterRimPointInRect(screenWidth - numberWidth, screenHeight - numberHeight, degrees);
        return new Point(outerRimPoint.x + (int) (numberWidth / 2), outerRimPoint.y + (int) (numberHeight / 2));
    }

    /**
     * @param degrees the angle of rotation about the center of the screen.  For example, for the position of the number "3" in an analog watchface, the angle is 0, and for the number "2", the angle is 30 degrees.
     * @return the coordinates of the point on the outermost location of the rectangular screen, relative to the upper-left corner of the screen.
     */
    @VisibleForTesting
    static Point getOuterRimPointInRect(double screenWidth, double screenHeight, double degrees) {
        // deltaX and deltaY represent the horizontal and vertical distance from the point to the center point in the screen.
        // The values are always positive.

        double deltaX = Math.min(screenWidth / 2, (screenHeight / 2) / Math.abs(Math.tan(Math.toRadians(degrees))));
        double deltaY = Math.min(screenHeight / 2, (screenWidth / 2) * Math.abs(Math.tan(Math.toRadians(degrees))));

        final double x;
        final double y;
        double normalizedDegrees = degrees % 360;
        if (normalizedDegrees < 0) normalizedDegrees += 360;
        if (normalizedDegrees > 90 && normalizedDegrees < 270) {
            x = screenWidth / 2 - deltaX;
        } else {
            x = screenWidth / 2 + deltaX;
        }
        if (normalizedDegrees < 180) {
            y = screenHeight / 2 - deltaY;
        } else {
            y = screenHeight / 2 + deltaY;
        }

        // The additional 0.5 is for rounding
        return new Point((int) (x + 0.5), (int) (y + 0.5));
    }

    /**
     * @param dialNumber   from 1 to 12.
     * @param numberWidth  the width of the image of the dial number
     * @param numberHeight the height of the image of the dial number
     * @return the position of the center of the dial number image, relative to the upper-left corner of the circular screen.
     */
    @VisibleForTesting
    static Point getDialNumberPositionInCircle(int dialNumber, double screenWidth, double numberWidth, double numberHeight) {
        double degrees = 90 - (dialNumber * 30);
        double dialNumberSize = Math.max(numberWidth, numberHeight);
        Point outerRimPoint = getOuterRimPointInCircle(screenWidth - dialNumberSize, degrees);
        return new Point(outerRimPoint.x + (int) (dialNumberSize / 2), outerRimPoint.y + (int) (dialNumberSize / 2));
    }

    /**
     * @param degrees the angle of rotation about the center of the screen.  For example, for the position of the number "3" in an analog watchface, the angle is 0, and for the number "2", the angle is 30 degrees.
     * @return the coordinates of the point on the outermost location of the circular screen, relative to the upper-left corner of the screen.
     */
    @VisibleForTesting
    static Point getOuterRimPointInCircle(double screenWidth, double degrees) {
        // the additional 0.5 is for rounding
        int x = (int) (screenWidth / 2 + (screenWidth / 2) * Math.cos(Math.toRadians(degrees)) + 0.5);
        int y = (int) (screenWidth / 2 - (screenWidth / 2) * Math.sin(Math.toRadians(degrees)) + 0.5);
        return new Point(x, y);
    }

}
