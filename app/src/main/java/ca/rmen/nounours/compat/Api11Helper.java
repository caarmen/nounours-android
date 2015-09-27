/*
 * Copyright (c) 2015 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours.compat;

import android.annotation.TargetApi;
import android.app.Activity;

@TargetApi(11)
public class Api11Helper {
    private Api11Helper() {
        // prevent instantiation
    }

    public static void invalidateOptionsMenu(Activity activity) {
        activity.invalidateOptionsMenu();
    }

}
