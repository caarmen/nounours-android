/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours.util;

import android.util.Log;

/**
 * Utility class for logging.
 *
 * @author Carmen Alvarez
 */
public class Trace {

    private static final String TAG = "Nounours/";
    public static void debug(Object context, Object o) {
        String className = context instanceof Class? ((Class)context).getSimpleName() : context.getClass().getSimpleName();
        Log.d(TAG + className, "" + o);
        if (o instanceof Throwable) {
            Log.d(TAG + className, ((Throwable) o).getMessage(), (Throwable) o);
        }
    }
}