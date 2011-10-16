/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours;

//import java.util.HashMap;

import android.app.Activity;
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
public class AndroidNounoursOnTouchListener implements OnTouchListener {

	private GestureDetector gestureDetector = null;
	private Nounours nounours = null;
/*
	private HashMap<Integer, Integer> lastXs = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> lastYs = new HashMap<Integer, Integer>();
	private int lastPointerId = -1;
*/
	public AndroidNounoursOnTouchListener(Nounours nounours, Activity activity,
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
		/*int pointerIndex = event.getActionIndex();
		int pointerId = event.getPointerId(pointerIndex);*/
		if (event.getAction() == MotionEvent.ACTION_DOWN
				/*|| event.getAction() == MotionEvent.ACTION_POINTER_DOWN*/) {
			/*int actionIndex = event.getActionIndex();
			int x = (int) event.getX(actionIndex);
			int y = (int) event.getY(actionIndex);*/
			//lastPointerId = pointerId;
			nounours.onPress((int) event.getX(), (int) event.getY());
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			nounours.onRelease();
			//lastPointerId = -1;
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			/*int pointerCount = event.getPointerCount();
			if (pointerCount == 1)*/
				nounours.onMove((int) event.getX(), (int) event.getY());
			/*else {
				for (int i = 0; i < event.getPointerCount(); i++) {
					int x = (int) event.getX(i);
					int y = (int) event.getY(i);
					int thisPointerId = event.getPointerId(i);
					Log.d("Hello", "this pointer id = " + thisPointerId
							+ "x: " + lastXs + ", y: " + lastYs);

					
					int lastX = getLastXForPointer(thisPointerId);
					int lastY = getLastYForPointer(thisPointerId);
					if (lastX != x || lastY != y) {
						Log.d("Hello", thisPointerId + " moved");
						lastXs.put(thisPointerId, x);
						lastYs.put(thisPointerId, y);
						nounours.onPress(x, y);
						lastPointerId = thisPointerId;
						nounours.onMove(x, y);
					}
				}
			}*/
		}

		// Store or remove pointer locations
		/*if (event.getAction() == MotionEvent.ACTION_DOWN
				|| event.getAction() == MotionEvent.ACTION_POINTER_DOWN
				|| event.getAction() == MotionEvent.ACTION_MOVE) {
			lastXs.put(pointerId, (int) event.getX(pointerIndex));
			lastYs.put(pointerId, (int) event.getY(pointerIndex));
		} else if (event.getAction() == MotionEvent.ACTION_UP
				|| event.getAction() == MotionEvent.ACTION_POINTER_UP) {
			lastXs.remove(pointerId);
			lastYs.remove(pointerId);
		}*/

		return true;
	}
	
	/*private int getLastXForPointer(int pointerId) {
		Integer value = lastXs.get(pointerId);
		if(value != null) return value.intValue();
		return -1;
	}
	private int getLastYForPointer(int pointerId) {
		Integer value = lastYs.get(pointerId);
		if(value != null) return value.intValue();
		return -1;
	}*/
}
