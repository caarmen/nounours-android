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

package ca.rmen.nounours.android.common.compat;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.drawable.Icon;

@TargetApi(23)
public class Api23Helper {
    private Api23Helper() {
        // prevent instantiation
    }

    public static int getColor(Context context, int id) {
        return context.getResources().getColor(id, null);
    }

    public static Notification createNotification(Context context, int iconId, String tickerText, String contentText, int actionIconId, CharSequence actionText, PendingIntent pendingIntent) {
        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle(tickerText)
                .setContentText(contentText)
                .setSmallIcon(iconId)
                .setContentIntent(pendingIntent);

        if (actionIconId > 0) {
            Icon icon = Icon.createWithResource(context, actionIconId);
            Notification.Action action = new Notification.Action.Builder(icon, actionText, pendingIntent).build();
            builder.addAction(action);
        }
        return builder.build();
    }

}
