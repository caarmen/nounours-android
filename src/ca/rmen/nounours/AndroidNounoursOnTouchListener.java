/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours;

import android.app.Activity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

/**
 * Manages touch events for Nounours on the Android device.
 *
 * @author Carmen Alvarez
 *
 */
public class AndroidNounoursOnTouchListener implements OnTouchListener {

    private GestureDetector gestureDetector = null;
    private Nounours nounours = null;
    private Activity activity = null;

    public AndroidNounoursOnTouchListener(Nounours nounours, Activity activity, GestureDetector gestureDetector) {
        this.nounours = nounours;
        this.activity = activity;
        this.gestureDetector = gestureDetector;
    }

    /**
     * The user touched, released, or moved.
     *
     * @see android.view.View.OnTouchListener#onTouch(android.view.View,
     *      android.view.MotionEvent)
     */
    @Override
    public boolean onTouch(final View v, final MotionEvent event) {

        final ImageView view = (ImageView) activity.findViewById(R.id.ImageView01);
        view.clearAnimation();
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(event);
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            nounours.onPress((int) event.getX(), (int) event.getY());
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            nounours.onRelease();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            nounours.onMove((int) event.getX(), (int) event.getY());
        }
        return true;
    }
}
