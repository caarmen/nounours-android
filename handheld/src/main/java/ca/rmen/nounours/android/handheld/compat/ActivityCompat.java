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

package ca.rmen.nounours.android.handheld.compat;

import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

import ca.rmen.nounours.android.common.compat.ApiHelper;

public class ActivityCompat {

    public static void invalidateOptionsMenu(Activity activity) {
        if (ApiHelper.getAPILevel() >= 11) {
            Api11Helper.invalidateOptionsMenu(activity);
        }
    }

    public static void setDisplayHomeAsUpEnabled(Activity activity) {
        if (ApiHelper.getAPILevel() >= 11) {
            Api11Helper.setDisplayHomeAsUpEnabled(activity);
        }
    }

    public static void setFullScreen(Activity activity, boolean isFullScreen) {
        Window window = activity.getWindow();
        if (isFullScreen) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        if (ApiHelper.getAPILevel() >= 19) {
            Api19Helper.setFullScreen(activity, isFullScreen);
        } else if (ApiHelper.getAPILevel() >= 16) {
            Api16Helper.setFullScreen(activity, isFullScreen);
        } else if (ApiHelper.getAPILevel() >= 14) {
            Api14Helper.setFullScreen(activity, isFullScreen);
        }

        invalidateOptionsMenu(activity);
    }
}
