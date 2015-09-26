/*
 * Copyright (c) 2015 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours.util;

import android.os.Build;

class NounoursApiHelper {
    private NounoursApiHelper() {
        // prevent instantiation
    }

    /**
     * On API Level 3, we need to read the SDK field, not the SDK_INT field.
     * This is isolated here to prevent many code insepction warnings about
     * "code maturity".
     */
    static int getAPILevel() {
        //noinspection deprecation
        return Integer.parseInt(Build.VERSION.SDK);
    }
}
