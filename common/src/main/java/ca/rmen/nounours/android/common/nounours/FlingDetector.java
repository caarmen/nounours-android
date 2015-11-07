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

package ca.rmen.nounours.android.common.nounours;

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

import ca.rmen.nounours.Nounours;

/**
 * Manages fling event for Nounours on the Android devices.
 *
 * @author Carmen Alvarez
 */
public class FlingDetector extends SimpleOnGestureListener {

    private final Nounours mNounours;

    public FlingDetector(Nounours nounours) {
        mNounours = nounours;
    }

    @Override
    public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
        mNounours.onFling((int) e1.getX(), (int) e1.getY(), velocityX, velocityY);
        return true;
    }

}
