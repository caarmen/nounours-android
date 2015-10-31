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

package ca.rmen.nounours;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Map;

import ca.rmen.nounours.compat.ActivityCompat;
import ca.rmen.nounours.compat.ApiHelper;
import ca.rmen.nounours.data.Animation;
import ca.rmen.nounours.data.Theme;
import ca.rmen.nounours.nounours.AndroidNounours;
import ca.rmen.nounours.nounours.FlingDetector;
import ca.rmen.nounours.nounours.TouchListener;
import ca.rmen.nounours.nounours.orientation.SensorListener;
import ca.rmen.nounours.settings.NounoursSettings;
import ca.rmen.nounours.settings.SettingsActivity;
import ca.rmen.nounours.util.AnimationUtil;
import ca.rmen.nounours.util.FileUtil;

/**
 * Android activity class which delegates Nounours-specific logic to the
 * {@link AndroidNounours} class.
 *
 * @author Carmen Alvarez
 */
public class MainActivity extends Activity {

    private static final String TAG = Constants.TAG + MainActivity.class.getSimpleName();

    private AndroidNounours mNounours;
    private SensorManager mSensorManager;
    private SensorListener mSensorListener;
    private Sensor mAccelerometerSensor;
    private Sensor mMagneticFieldSensor;
    private ImageButton mRecordButton;
    private ProgressDialog mProgressDialog;


    /**
     * Initialize Nounours (read the CSV data files, register as a listener for
     * touch events).
     *
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        boolean isOldEmulator = Build.DEVICE.startsWith("generic") && ApiHelper.getAPILevel() == 3;
        if (!isOldEmulator) {
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        }


        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mRecordButton = (ImageButton) findViewById(R.id.btn_stop_recording);
        mRecordButton.setOnClickListener(mOnClickListener);

        mNounours = new AndroidNounours("APP",
                MainActivity.this,
                new Handler(),
                NounoursSettings.getAppSettings(MainActivity.this),
                surfaceView.getHolder(),
                mListener);

        FlingDetector nounoursFlingDetector = new FlingDetector(mNounours);
        if (mSensorManager != null) {
            mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mMagneticFieldSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }

        final GestureDetector gestureDetector = new GestureDetector(getApplicationContext(), nounoursFlingDetector);
        TouchListener touchListener = new TouchListener(mNounours, gestureDetector);
        surfaceView.setOnTouchListener(touchListener);
        mSensorListener = new SensorListener(mNounours, getApplicationContext());

        if (ApiHelper.getAPILevel() < 11) {
            Toast.makeText(this, R.string.toast_remindMenuButton, Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Called when the application is started or becomes active. Register for
     * sensor events, enable pinging for idle context, and call
     * mNounours.onResume().
     *
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        Log.v(TAG, "onResume begin");

        super.onResume();

        if (mNounours == null) return;
        mNounours.reloadSettings();
        mNounours.doPing(true);
        Log.v(TAG, "onResume end");
        if (mSensorManager != null && mSensorListener != null) {
            mSensorManager.registerListener(mSensorListener, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(mSensorListener, mMagneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        registerReceiver(mBroadcastReceiver, new IntentFilter(AnimationSaveService.ACTION_SAVE_ANIMATION));
    }

    /**
     * The application is paused. Stop listening for sensor events, stop pinging
     * for idleness, stop any sound.
     *
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
        mNounours.doPing(false);
        mNounours.stopSound();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mSensorListener);
        }
        unregisterReceiver(mBroadcastReceiver);
    }


    /**
     * Create menu items for the different animations.
     *
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    private void setupAnimationMenu(SubMenu actionMenu) {
        actionMenu.clear();
        getMenuInflater().inflate(R.menu.animation_menu, actionMenu);
        int actionMenuIdx = 1;
        // All the animations
        final Map<String, Animation> animations = mNounours.getAnimations();
        for (final Animation animation : animations.values()) {
            if (animation.isVisible()) {
                final int animationId = Integer.parseInt(animation.getId());
                String animationLabel = animation.getLabel();
                int labelIdx = getResources()
                        .getIdentifier(animationLabel, "string", getPackageName());
                if (labelIdx > 0)
                    actionMenu.add(Menu.NONE, animationId, actionMenuIdx++, labelIdx);
                else
                    actionMenu.add(Menu.NONE, animationId, actionMenuIdx++, animationLabel);
            }
        }
    }

    /**
     * Disable/enable any menu items.
     */
    public boolean onPrepareOptionsMenu(final Menu menu) {
        // Prevent changing the theme in the middle of the animation.
        if (mNounours != null) {
            Theme theme = mNounours.getCurrentTheme();
            boolean nounoursIsBusy = mNounours.isAnimationRunning() || mNounours.isLoading();
            MenuItem animationMenu = menu.findItem(R.id.menu_animation);
            if (animationMenu != null) {
                animationMenu.setEnabled(!nounoursIsBusy);
                boolean hasAnimations = theme != null && !theme.getAnimations().isEmpty();
                animationMenu.setVisible(hasAnimations);
                if (hasAnimations) setupAnimationMenu(animationMenu.getSubMenu());
            }
            MenuItem recordingMenu = menu.findItem(R.id.menu_start_recording);
            if (recordingMenu != null) {
                recordingMenu.setEnabled(FileUtil.isSdPresent() && !mNounours.getNounoursRecorder().isRecording());
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Handle menu item selections.
     *
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.menu_options) {
            SettingsActivity.startAppSettingsActivity(this);
            return true;
        }
        // Show the help
        else if (menuItem.getItemId() == R.id.menu_help) {
            mNounours.onHelp();
            return true;
        }
        // The user picked the random animation
        else if (menuItem.getItemId() == R.id.menu_random_animation) {
            mNounours.doRandomAnimation();
            return true;
        } else if (menuItem.getItemId() == R.id.menu_start_recording) {
            startRecording();
            ActivityCompat.invalidateOptionsMenu(this);
            return true;
        } else if (menuItem.getItemId() == R.id.menu_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }
        // Show an animation or change the theme.
        else {
            final Map<String, Animation> animations = mNounours.getAnimations();
            final Animation animation = animations.get("" + menuItem.getItemId());
            if (animation != null) {
                mNounours.doAnimation(animation);
                return true;
            }
            return super.onOptionsItemSelected(menuItem);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        mNounours.onDestroy();
        super.onDestroy();
    }

    private void startRecording() {
        AnimationUtil.startAnimation(mRecordButton);
        mNounours.getNounoursRecorder().start();
    }

    private void stopRecording() {
        Toast.makeText(this, R.string.notif_save_animation_in_progress_title, Toast.LENGTH_LONG).show();
        AnimationUtil.stopAnimation(mRecordButton);
        Animation animation = mNounours.getNounoursRecorder().stop();
        AnimationSaveService.startActionSaveAnimation(this, animation);
    }

    private final AndroidNounours.AndroidNounoursListener mListener = new AndroidNounours.AndroidNounoursListener() {
        @Override
        public void onThemeLoadStart(int max, String message) {
            Log.v(TAG, "onThemeLoadStart: max=" + max + ", message = " + message);
            createProgressDialog(max, message);
        }

        @Override
        public void onThemeLoadProgress(int progress, int max, String message) {
            Log.v(TAG, "onThemeLoadProgress: progress=" + progress + ", max=" + max + ", message=" + message);
            // show the progress bar if it is not already showing.
            if (mProgressDialog == null || !mProgressDialog.isShowing())
                createProgressDialog(max, message);
            // Update the progress
            mProgressDialog.setProgress(progress);
            mProgressDialog.setMax(max);
            mProgressDialog.setMessage(message);
            if (progress == max) mProgressDialog.dismiss();
        }

        @Override
        public void onThemeLoadComplete() {
            Log.v(TAG, "onThemeLoadComplete");
            mSensorListener.rereadOrientationFile(MainActivity.this);
            mProgressDialog.dismiss();
            ActivityCompat.invalidateOptionsMenu(MainActivity.this);
        }

        /**
         * Create a determinate progress dialog with the given size and text.
         */
        private void createProgressDialog(int max, String message) {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(MainActivity.this);
                mProgressDialog.setTitle("");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setProgress(0);
                mProgressDialog.setCancelable(false);
            }
            mProgressDialog.setMessage(message);
            mProgressDialog.setIndeterminate(max < 0);
            mProgressDialog.setMax(max);
            mProgressDialog.show();
            Log.v(TAG, "createProgressDialog " + max + ": " + message);
        }
    };

    /**
     * When the user taps on the record button, we stop recording.
     */
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.btn_stop_recording) {
                stopRecording();
            }
        }
    };

    /**
     * When the file has been saved, let's prompt the user to share it.
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AnimationSaveService.ACTION_SAVE_ANIMATION.equals(intent.getAction())) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                // We clear the notification because we don't need it here: we will directly
                // prompt the user to share the file.  (The notification is useful if the
                // file saving takes a long time, and the user leaves the activity in the middle
                // of the saving.  In that case, the user will have to tap on the notification
                // see the share app list.
                notificationManager.cancel(AnimationSaveService.NOTIFICATION_ID);
                Intent shareIntent = intent.getParcelableExtra(AnimationSaveService.EXTRA_SHARE_INTENT);
                startActivity(shareIntent);
            }
        }
    };

}
