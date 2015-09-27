/*
 *   Copyright (c) 2009 - 2015 Carmen Alvarez
 *
 *   This file is part of Nounours for Android.
 *
 *   Nounours for Android is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Nounours for Android is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Nounours for Android.  If not, see <http://www.gnu.org/licenses/>.
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
