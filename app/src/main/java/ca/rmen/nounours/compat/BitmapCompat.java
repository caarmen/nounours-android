/*
 * Copyright (c) 2015 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours.compat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

public class BitmapCompat {

    public static BitmapDrawable createBitmapDrawable(Context context, Bitmap bitmap) {
        if (ApiHelper.getAPILevel() >= 4) {
            return Api4Helper.createBitmapDrawable(context.getResources(), bitmap);
        }
        //noinspection deprecation
        return new BitmapDrawable(bitmap);

    }

    public static BitmapFactory.Options createBitmapFactoryOptions(int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        if (ApiHelper.getAPILevel() >= 4) {
            Api4Helper.setBitmapFactoryOptions(options);
        }
        return options;
    }
}
