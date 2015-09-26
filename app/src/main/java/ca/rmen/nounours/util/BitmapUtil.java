/*
This source is part of the
     _____  ___   ____
 __ / / _ \/ _ | / __/___  _______ _
/ // / , _/ __ |/ _/_/ _ \/ __/ _ `/
\___/_/|_/_/ |_/_/ (_)___/_/  \_, /
                             /___/
repository. It is licensed under a Creative Commons
Attribution-Noncommercial-Share Alike 3.0 Unported License:
http://creativecommons.org/licenses/by-nc-sa/3.0.
Contact BoD@JRAF.org for more information.

$Id: FileUtil.java 625 2009-04-26 22:45:17Z bod $
 */
package ca.rmen.nounours.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

import ca.rmen.nounours.compat.BitmapCompat;

public class BitmapUtil {
    private static final int BITMAP_LOAD_RETRIES = 3;
    private static final int BITMAP_INITIAL_SUB_SAMPLE = 0;

    public static Bitmap loadBitmap(Context context, String filename) {
        return loadBitmap(context, new File(filename), 0, BITMAP_INITIAL_SUB_SAMPLE, BITMAP_LOAD_RETRIES);
    }

    public static Bitmap loadBitmap(Context context, int resourceId) {
        return loadBitmap(context, null, resourceId, BITMAP_INITIAL_SUB_SAMPLE, BITMAP_LOAD_RETRIES);
    }

    private static Bitmap loadBitmap(Context context, File file, int resourceId, int initialSubSample, int retries) {
        int inSampleSize = BITMAP_LOAD_RETRIES - retries + initialSubSample;
        BitmapFactory.Options options = BitmapCompat.createBitmapFactoryOptions(inSampleSize);
        try {
            Trace.debug(BitmapUtil.class, "Load image " + (file == null ? "" + resourceId : file.getAbsolutePath()) + ".  "
                    + retries + " left.  Sample size = " + options.inSampleSize);

            if (file != null)
                return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            else
                return BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        } catch (OutOfMemoryError e) {
            System.gc();
            if (retries > 0)
                return loadBitmap(context, file, resourceId, initialSubSample, retries - 1);
        }
        return null;
    }
}
