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

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.view.View;
import android.view.Window;

@TargetApi(16)
class Api16Helper {
    private Api16Helper() {
        // prevent instantiation
    }

    static Notification createNotification(Context context, int iconId, String tickerText, String contentText, int actionIconId, CharSequence actionText, PendingIntent pendingIntent) {
        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle(tickerText)
                .setContentText(contentText)
                .setSmallIcon(iconId)
                .setContentIntent(pendingIntent);

        if (actionIconId > 0) {
            //noinspection deprecation
            builder.addAction(actionIconId, actionText, pendingIntent);
        }
        return builder.build();
    }

    static void setFullScreen(Window window, boolean isFullScreen) {
        if (isFullScreen) {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LOW_PROFILE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        } else {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_VISIBLE);

        }
    }

}
