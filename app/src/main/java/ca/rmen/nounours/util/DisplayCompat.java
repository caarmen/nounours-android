/*
 * Copyright (c) 2015 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours.util;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

public class DisplayCompat {

    public static int getWidth(Context context) {
        final WindowManager wm = (WindowManager) context
                .getApplicationContext().getSystemService(
                        Context.WINDOW_SERVICE);
        final Display display = wm.getDefaultDisplay();
        if (NounoursApiHelper.getAPILevel() >= 13) {
            return NounoursApi13Helper.getWidth(display);
        }
        //noinspection deprecation
        return display.getWidth();

    }

    public static int getHeight(Context context) {
        final WindowManager wm = (WindowManager) context
                .getApplicationContext().getSystemService(
                        Context.WINDOW_SERVICE);
        final Display display = wm.getDefaultDisplay();
        if (NounoursApiHelper.getAPILevel() >= 13) {
            return NounoursApi13Helper.getHeight(display);
        }
        //noinspection deprecation
        return display.getHeight();

    }

    public static int getRotation(Context context) {
        final WindowManager wm = (WindowManager) context
                .getApplicationContext().getSystemService(
                        Context.WINDOW_SERVICE);
        final Display display = wm.getDefaultDisplay();
        if (NounoursApiHelper.getAPILevel() >= 8) {
            return NounoursApi8Helper.getRotation(display);
        }
        //noinspection deprecation
        return display.getOrientation();
    }
}
