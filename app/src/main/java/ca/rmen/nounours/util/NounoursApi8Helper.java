/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours.util;

import android.annotation.TargetApi;
import android.view.Display;

@TargetApi(8)
class NounoursApi8Helper {
    private NounoursApi8Helper() {
        // prevent instantiation
    }

    static int getRotation(Display display) {
        return display.getRotation();
    }
}
