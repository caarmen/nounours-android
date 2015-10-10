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
import android.net.Uri;
import android.util.Log;

import java.io.File;

import ca.rmen.nounours.compat.NotificationCompat;
import ca.rmen.nounours.data.Animation;
import ca.rmen.nounours.util.AnimationUtil;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * This service saves an animation file to the disk.  It displays a notification to indicate
 * the progress of the saving, and at the end, it prompts the user to choose an app to share
 * the animation.
 */
public class AnimationSaveService extends IntentService {
    private static final String TAG = Constants.TAG + AnimationSaveService.class.getSimpleName();

    public static final String ACTION_SAVE_ANIMATION = "ca.rmen.nounours.action.SAVE_ANIMATION";
    public static final String EXTRA_SHARE_INTENT = "ca.rmen.nounours.extra.SHARE_INTENT";
    public static final int NOTIFICATION_ID = TAG.hashCode();

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

    @SuppressWarnings("WeakerAccess")
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
        final int iconId = R.drawable.ic_stat_content_save;

        // Notify that the save is in progress
        Notification notification = NotificationCompat.createNotification(this, iconId, R.string.notif_save_animation_in_progress_title, R.string.notif_save_animation_in_progress_content, getMainActivityIntent());
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);

        // Save the file
        File file = AnimationUtil.saveAnimation(this, animation);

        // Notify based on the save result.
        if(file != null && file.exists()) {
            // Notify that the save is done.
            Intent shareIntent = getShareIntent(file);
            PendingIntent pendingShareIntent = PendingIntent.getActivity(this, 0, shareIntent, 0);
            notification = NotificationCompat.createNotification(this, iconId, R.string.notif_save_animation_done, R.string.notif_save_animation_done, pendingShareIntent);
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(NOTIFICATION_ID, notification);
            // Also broadcast that the save is done.
            Intent broadcastIntent = new Intent(ACTION_SAVE_ANIMATION);
            broadcastIntent.putExtra(EXTRA_SHARE_INTENT, shareIntent);
            sendBroadcast(broadcastIntent);
        } else {
            // Notify that the save failed.
            notification = NotificationCompat.createNotification(this, iconId, R.string.notif_save_animation_failed, R.string.notif_save_animation_failed, getMainActivityIntent());
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(NOTIFICATION_ID, notification);
        }

        Log.v(TAG, "end saving animation " + animation);
    }

    /**
     * While the saving is in progress, we need a PendingIntent for the notification. We'll
     * just have a PendingIntent which launches the MainActivity.
     */
    private PendingIntent getMainActivityIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }

    /**
     * @return an Intent to show a chooser of apps to share a file.
     */
    private Intent getShareIntent(File file) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.getAbsolutePath()));
        sendIntent.setType("image/gif");
        sendIntent.setFlags(PendingIntent.FLAG_CANCEL_CURRENT);
        return Intent.createChooser(sendIntent, getString(R.string.share_app_chooser_title));
    }
}
