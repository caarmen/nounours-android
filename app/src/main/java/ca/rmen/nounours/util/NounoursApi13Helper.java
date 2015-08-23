/*
 * Copyright (c) 2015 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours.util;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.view.Display;

@TargetApi(13)
class NounoursApi13Helper {
    private NounoursApi13Helper() {
        // Prevent instantiation
    }

    static int getWidth(Display display) {
        Point point = new Point();
        display.getSize(point);
        return point.x;
    }

    static int getHeight(Display display) {
        Point point = new Point();
        display.getSize(point);
        return point.y;
    }

}
