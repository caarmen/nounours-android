/*
 * Copyright (c) 2015 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;

@SuppressWarnings("deprecation")
public class BitmapDrawableCompat {

    public static BitmapDrawable createBitmapDrawable(Context context, Bitmap bitmap) {
        if (Integer.parseInt(Build.VERSION.SDK) >= 4) {
            return NounoursApi4Helper.createBitmapDrawable(context.getResources(), bitmap);
        }
        return new BitmapDrawable(bitmap);

    }

    static BitmapFactory.Options createBitmapFactoryOptions(int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        if (Integer.parseInt(Build.VERSION.SDK) >= 4) {
            NounoursApi4Helper.setBitmapFactoryOptions(options);
        }
        return options;
    }
}
