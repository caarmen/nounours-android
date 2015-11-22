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

package ca.rmen.nounours.android.handheld.compat;

import android.view.Window;
import android.view.WindowManager;

import ca.rmen.nounours.android.common.compat.ApiHelper;

public class WindowCompat {

    public static void setFullScreen(Window window, boolean isFullScreen) {
        if (isFullScreen) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        if (ApiHelper.getAPILevel() >= 19) {
            Api19Helper.setFullScreen(window, isFullScreen);
        } else if (ApiHelper.getAPILevel() >= 16) {
            Api16Helper.setFullScreen(window, isFullScreen);
        } else if (ApiHelper.getAPILevel() >= 14) {
            Api14Helper.setFullScreen(window, isFullScreen);
        }
    }
}
