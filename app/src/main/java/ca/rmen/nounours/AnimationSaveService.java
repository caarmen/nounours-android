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

package ca.rmen.nounours;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.util.Log;

import java.io.File;

import ca.rmen.nounours.compat.NotificationCompat;
import ca.rmen.nounours.data.Animation;
import ca.rmen.nounours.nounours.cache.AnimationCache;
import ca.rmen.nounours.util.AnimationUtil;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class AnimationSaveService extends IntentService {
    private static final String TAG = Constants.TAG + AnimationSaveService.class.getSimpleName();

    private static final String ACTION_SAVE_ANIMATION = "ca.rmen.nounours.action.SAVE_ANIMATION";

    private static final String EXTRA_ANIMATION = "ca.rmen.nounours.extra.ANIMATION";

    /**
     * Starts this service to perform action SaveAnimation with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionSaveAnimation(Context context, Animation animation) {
        Intent intent = new Intent(context, AnimationSaveService.class);
        intent.setAction(ACTION_SAVE_ANIMATION);
        intent.putExtra(EXTRA_ANIMATION, animation);
        context.startService(intent);
    }

    public AnimationSaveService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SAVE_ANIMATION.equals(action)) {
                final Animation animation = (Animation) intent.getSerializableExtra(EXTRA_ANIMATION);
                handleActionSaveAnimation(animation);
            }
        }
    }

    /**
     * Handle action SaveAnimation in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSaveAnimation(Animation animation) {
        Log.v(TAG, "begin saving animation " + animation);
        final int notificationId = TAG.hashCode();
        final int iconId = R.drawable.icon;

        // Notify that the save is in progress
        Notification notification = NotificationCompat.createNotification(this, iconId, R.string.notif_save_animation_in_progress, R.string.notif_save_animation_in_progress, getMainActivityIntent());
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notification);

        // Save the file
        final AnimationDrawable animationDrawable = AnimationCache.getInstance().createAnimation(this, animation);
        File file = AnimationUtil.saveAnimation(this, animationDrawable, animation.getId());

        // Notify that the save is done.
        notification = NotificationCompat.createNotification(this, iconId, R.string.notif_save_animation_done, R.string.notif_save_animation_done, getShareIntent(file));
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(notificationId, notification);
        Log.v(TAG, "end saving animation " + animation);
    }

    private PendingIntent getMainActivityIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }

    private PendingIntent getShareIntent(File file) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.getAbsolutePath()));
        sendIntent.setType("image/gif");

        Intent chooserIntent = Intent.createChooser(sendIntent, getString(R.string.share_app_chooser_title));
        return PendingIntent.getActivity(this, 0, chooserIntent, 0);
    }
}
