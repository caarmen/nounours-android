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

    public static void debug(Object context, Object o) {
        Log.d(context.getClass().getName(), "" + o);
        if (o instanceof Throwable) {
            Log.d(context.getClass().getName(), ((Throwable) o).getMessage(), (Throwable) o);
        }
    }
}