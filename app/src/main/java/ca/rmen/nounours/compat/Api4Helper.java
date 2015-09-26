/*
 * Copyright (c) 2015 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours.compat;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

@TargetApi(4)
public class Api4Helper {
    private Api4Helper() {
        // prevent instantiation
    }

    public static BitmapDrawable createBitmapDrawable(Resources resources, Bitmap bitmap) {
        return new BitmapDrawable(resources, bitmap);
    }

    public static void setBitmapFactoryOptions(BitmapFactory.Options options) {
        options.inPurgeable = true;
        options.inInputShareable = true;
    }
}
