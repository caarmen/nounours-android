/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours.nounours;

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

import ca.rmen.nounours.Nounours;

/**
 * Manages fling event for Nounours on the Android devices.
 *
 * @author Carmen Alvarez
 */
public class FlingDetector extends SimpleOnGestureListener {

    private Nounours nounours = null;

    public FlingDetector(Nounours nounours) {
        this.nounours = nounours;
    }

    @Override
    public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
        nounours.onFling((int) e1.getX(), (int) e1.getY(), velocityX, velocityY);
        return true;
    }

}
