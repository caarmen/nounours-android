/*
 * Copyright (c) 2015 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours.compat;

import android.app.Activity;

public class ActivityCompat {

    public static void invalidateOptionsMenu(Activity activity) {
        if (ApiHelper.getAPILevel() >= 11) {
            Api11Helper.invalidateOptionsMenu(activity);
        }
    }
}
