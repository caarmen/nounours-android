/*
 * Copyright (c) 2015 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours.compat;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.view.Display;

@TargetApi(13)
public class Api13Helper {
    private Api13Helper() {
        // Prevent instantiation
    }

    public static int getWidth(Display display) {
        Point point = new Point();
        display.getSize(point);
        return point.x;
    }

    public static int getHeight(Display display) {
        Point point = new Point();
        display.getSize(point);
        return point.y;
    }

}
