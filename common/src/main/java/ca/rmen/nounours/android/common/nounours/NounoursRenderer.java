package ca.rmen.nounours.android.common.nounours;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

import ca.rmen.nounours.android.common.settings.NounoursSettings;

public class NounoursRenderer {
    public void render(NounoursSettings settings,
                       Bitmap bitmap,
                       Canvas c,
                       int viewWidth, int viewHeight) {
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
        c.drawColor(settings.getBackgroundColor());
        Matrix m = new Matrix();
        m.postTranslate(offsetX, offsetY);
        m.postScale(scaleToUse, scaleToUse, deviceCenterX, deviceCenterY);
        c.setMatrix(m);
        c.drawBitmap(bitmap, 0, 0, null);
        if (settings.isImageDimmed()) c.drawColor(0x88000000);
    }
}
