/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours;

//import java.util.HashMap;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * Manages touch events for Nounours on the Android device.
 * 
 * @author Carmen Alvarez
 * 
 */
class AndroidNounoursOnTouchListener implements OnTouchListener {

	private GestureDetector gestureDetector = null;
	private Nounours nounours = null;
	public AndroidNounoursOnTouchListener(Nounours nounours,
			GestureDetector gestureDetector) {
		this.nounours = nounours;
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
