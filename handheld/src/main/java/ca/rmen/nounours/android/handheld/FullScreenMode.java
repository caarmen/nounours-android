/*
 *   Copyright (c) 2015 Carmen Alvarez
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
package ca.rmen.nounours.android.handheld;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;

import ca.rmen.nounours.android.handheld.compat.ActivityCompat;

/**
 * The activity can enter full-screen mode when the user selects the option from the menu.
 * This class places the screen into full-screen mode.
 * To exit the full-screen mode, the user must tap the four corners, in any order.
 * This class keeps track of the taps to the four corners, and exits full-screen mode.
 * This class is also responsible for invalidating the activity's options menu when
 * entering or exiting full screen mode.
 */
class FullScreenMode {
    private final Activity mActivity;
    private final View mCorner1, mCorner2, mCorner3, mCorner4;
    private final View mViewFullScreenHint;

    private final long[] mCornerTouchTimestamps = new long[]{0, 0, 0, 0};
    private static final long UNLOCK_DELAY_MS = 10000;
    private boolean mIsInFullScreen;

    FullScreenMode(Activity activity, View corner1, View corner2, View corner3, View corner4, View fullScreenHint) {
        mActivity = activity;
        mCorner1 = corner1;
        mCorner2 = corner2;
        mCorner3 = corner3;
        mCorner4 = corner4;
        mViewFullScreenHint = fullScreenHint;
        CornerTouchListener cornerTouchListener = new CornerTouchListener();
        mCorner1.setOnTouchListener(cornerTouchListener);
        mCorner2.setOnTouchListener(cornerTouchListener);
        mCorner3.setOnTouchListener(cornerTouchListener);
        mCorner4.setOnTouchListener(cornerTouchListener);
    }

    boolean isInFullScreen() {
        return mIsInFullScreen;
    }

    void enterFullScreen() {
        mIsInFullScreen = true;
        mCorner1.setVisibility(View.VISIBLE);
        mCorner2.setVisibility(View.VISIBLE);
        mCorner3.setVisibility(View.VISIBLE);
        mCorner4.setVisibility(View.VISIBLE);
        mViewFullScreenHint.setVisibility(View.VISIBLE);
        ActivityCompat.setFullScreen(mActivity, true);
        for (int i = 0; i < mCornerTouchTimestamps.length; i++) mCornerTouchTimestamps[i] = 0;
    }

    void exitFullScreen() {
        mIsInFullScreen = false;
        mCorner1.setVisibility(View.GONE);
        mCorner2.setVisibility(View.GONE);
        mCorner3.setVisibility(View.GONE);
        mCorner4.setVisibility(View.GONE);
        mViewFullScreenHint.setVisibility(View.GONE);
        ActivityCompat.setFullScreen(mActivity, false);
    }

    private boolean haveCornersAllBeenTappedRecently() {
        long oldestPossibleTimestamp = System.currentTimeMillis() - UNLOCK_DELAY_MS;
        for (long cornerTimestamp : mCornerTouchTimestamps) {
            if (cornerTimestamp < oldestPossibleTimestamp) return false;
        }
        return true;
    }

    private class CornerTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mIsInFullScreen) {
                long now = System.currentTimeMillis();
                if (v.getId() == mCorner1.getId()) {
                    mCornerTouchTimestamps[0] = now;
                } else if (v.getId() == mCorner2.getId()) {
                    mCornerTouchTimestamps[1] = now;
                } else if (v.getId() == mCorner3.getId()) {
                    mCornerTouchTimestamps[2] = now;
                } else if (v.getId() == mCorner4.getId()) {
                    mCornerTouchTimestamps[3] = now;
                }
                if (haveCornersAllBeenTappedRecently()) {
                    exitFullScreen();
                }
            }
            return false;
        }
    }
}
