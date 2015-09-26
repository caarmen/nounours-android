/*
 * Copyright (c) 2015 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours.compat;

import android.os.Build;

public class ApiHelper {
    private ApiHelper() {
        // prevent instantiation
    }

    /**
     * On API Level 3, we need to read the SDK field, not the SDK_INT field.
     * This is isolated here to prevent many code insepction warnings about
     * "code maturity".
     */
    public static int getAPILevel() {
        //noinspection deprecation
        return Integer.parseInt(Build.VERSION.SDK);
    }
}
