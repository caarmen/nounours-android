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
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.drawable.Icon;

import ca.rmen.nounours.R;

@TargetApi(26)
class Api26Helper {
    private static final String CHANNEL_ID = "NOUNOURS";
    private Api26Helper() {
        // prevent instantiation
    }

    static Notification createNotification(Context context, int iconId, String tickerText, String contentText, int actionIconId, CharSequence actionText, PendingIntent pendingIntent) {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                context.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);

        Notification.Builder builder = new Notification.Builder(context, CHANNEL_ID)
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
