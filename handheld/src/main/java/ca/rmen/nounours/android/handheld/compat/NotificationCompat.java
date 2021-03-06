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

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ca.rmen.nounours.android.common.Constants;
import ca.rmen.nounours.android.common.compat.Api23Helper;
import ca.rmen.nounours.android.common.compat.ApiHelper;

public final class NotificationCompat {

    private static final String TAG = Constants.TAG + NotificationCompat.class.getSimpleName();

    private NotificationCompat() {
        // prevent instantiation
    }

    public static Notification createNotification(Context context, int iconId, int tickerTextResId, int contentTextResId, PendingIntent pendingIntent) {
        return createNotification(context, iconId, tickerTextResId, contentTextResId, 0, null, pendingIntent);
    }

    public static Notification createNotification(Context context, int iconId, int tickerTextResId, int contentTextResId, int actionIconId, CharSequence actionText, PendingIntent pendingIntent) {
        String tickerText = context.getString(tickerTextResId);
        String contentText = context.getString(contentTextResId);
        if (ApiHelper.getAPILevel() < 11) {
            Notification notification = new Notification();
            notification.tickerText = tickerText;
            notification.when = System.currentTimeMillis();
            //noinspection deprecation
            notification.icon = iconId;
            notification.contentIntent = pendingIntent;
            // Google removed setLatestEventInfo in sdk 23.
            //noinspection TryWithIdenticalCatches
            try {
                Method method = Notification.class.getMethod("setLatestEventInfo", Context.class, CharSequence.class, CharSequence.class, PendingIntent.class);
                method.invoke(notification, context, tickerText, contentText, pendingIntent);
            } catch (NoSuchMethodException e) {
                Log.v(TAG, e.getMessage(), e);
            } catch (IllegalAccessException e) {
                Log.v(TAG, e.getMessage(), e);
            } catch (InvocationTargetException e) {
                Log.v(TAG, e.getMessage(), e);
            }
            return notification;
        } else if (ApiHelper.getAPILevel() < 16) {
            return Api11Helper.createNotification(context, iconId, tickerText, contentText, pendingIntent);
        } else if (ApiHelper.getAPILevel() < 23){
            return Api16Helper.createNotification(context, iconId, tickerText, contentText, actionIconId, actionText, pendingIntent);
        } else if (ApiHelper.getAPILevel() < 26){
            return Api23Helper.createNotification(context, iconId, tickerText, contentText, actionIconId, actionText, pendingIntent);
        } else {
            return Api26Helper.createNotification(context, iconId, tickerText, contentText, actionIconId, actionText, pendingIntent);
        }
    }

}
