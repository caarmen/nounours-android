/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours;

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

/**
 * Manages fling event for Nounours on the Android devices.
 *
 * @author Carmen Alvarez
 */
class AndroidNounoursGestureDetector extends SimpleOnGestureListener {

    private Nounours nounours = null;

    public AndroidNounoursGestureDetector(Nounours nounours) {
        this.nounours = nounours;
    }

    @Override
    public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
        nounours.onFling((int) e1.getX(), (int) e1.getY(), velocityX, velocityY);
        return true;
    }

}
