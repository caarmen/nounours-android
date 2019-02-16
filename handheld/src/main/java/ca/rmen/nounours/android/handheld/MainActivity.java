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

package ca.rmen.nounours.android.handheld;

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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Map;

import ca.rmen.nounours.R;
import ca.rmen.nounours.android.common.Constants;
import ca.rmen.nounours.android.common.compat.ApiHelper;
import ca.rmen.nounours.android.common.nounours.AndroidNounours;
import ca.rmen.nounours.android.common.nounours.NounoursRenderer;
import ca.rmen.nounours.android.common.nounours.ThemeLoadListener;
import ca.rmen.nounours.android.common.nounours.cache.ImageCache;
import ca.rmen.nounours.android.common.nounours.cache.NounoursResourceCache;
import ca.rmen.nounours.android.common.nounours.cache.SoundCache;
import ca.rmen.nounours.android.common.settings.NounoursSettings;
import ca.rmen.nounours.android.handheld.compat.ActivityCompat;
import ca.rmen.nounours.android.handheld.nounours.FlingDetector;
import ca.rmen.nounours.android.handheld.nounours.SoundHandler;
import ca.rmen.nounours.android.handheld.nounours.TouchListener;
import ca.rmen.nounours.android.handheld.nounours.VibrateHandler;
import ca.rmen.nounours.android.handheld.nounours.orientation.SensorListener;
import ca.rmen.nounours.android.handheld.settings.SettingsActivity;
import ca.rmen.nounours.android.handheld.settings.SharedPreferenceSettings;
import ca.rmen.nounours.android.handheld.util.AnimationUtil;
import ca.rmen.nounours.data.Animation;
import ca.rmen.nounours.data.Theme;

/**
 * Android activity class which delegates Nounours-specific logic to the
 * {@link AndroidNounours} class.
 *
 * @author Carmen Alvarez
 */
public class MainActivity extends Activity {

    private static final String TAG = Constants.TAG + MainActivity.class.getSimpleName();

    private static final String FLAG_FULLSCREEN = "fullscreen";

    private AndroidNounours mNounours;
    private SensorManager mSensorManager;
    private SensorListener mSensorListener;
    private Sensor mAccelerometerSensor;
    private Sensor mMagneticFieldSensor;
    private ImageButton mRecordButton;
    private ProgressDialog mProgressDialog;
    private FullScreenMode mFullScreenMode;


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

        boolean isOldEmulator = Build.DEVICE.startsWith("generic") && ApiHelper.getAPILevel() < 9;
        if (!isOldEmulator) {
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        }

        mFullScreenMode = new FullScreenMode(this,
                findViewById(R.id.corner1),
                findViewById(R.id.corner2),
                findViewById(R.id.corner3),
                findViewById(R.id.corner4),
                findViewById(R.id.fullscreen_hint));

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        final SurfaceView surfaceView = findViewById(R.id.surface_view);
        mRecordButton = findViewById(R.id.btn_stop_recording);
        mRecordButton.setOnClickListener(mOnClickListener);
        ImageCache imageCache = new ImageCache();
        SoundCache soundCache = new SoundCache();
        SoundHandler soundHandler = new SoundHandler(this, soundCache);
        VibrateHandler vibrateHandler = new VibrateHandler(this);
        NounoursSettings settings = SharedPreferenceSettings.getAppSettings(this);
        NounoursResourceCache nounoursResources = new NounoursResourceCache(this, settings, imageCache, soundCache);
        NounoursRenderer renderer = new NounoursRenderer();

        mNounours = new AndroidNounours("APP",
                MainActivity.this,
                new Handler(),
                settings,
                surfaceView.getHolder(),
                renderer,
                nounoursResources,
                soundHandler,
                vibrateHandler,
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(FLAG_FULLSCREEN, mFullScreenMode.isInFullScreen());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean isInFullScreen = savedInstanceState.getBoolean(FLAG_FULLSCREEN);
        if (isInFullScreen) {
            mFullScreenMode.enterFullScreen();
        } else {
            mFullScreenMode.exitFullScreen();
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
        boolean isFullScreen = mFullScreenMode.isInFullScreen();
        // Hide all menu items in full-screen mode.
        for (int i=0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(!isFullScreen);
        }
        // Prevent changing the theme in the middle of the animation.
        if (mNounours != null && !isFullScreen) {
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
                recordingMenu.setEnabled(!mNounours.getNounoursRecorder().isRecording());
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
        } else if (menuItem.getItemId() == R.id.menu_fullscreen) {
            mFullScreenMode.enterFullScreen();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //noinspection SimplifiableIfStatement
        if (mFullScreenMode.isInFullScreen() && keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
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

    private final ThemeLoadListener mListener = new ThemeLoadListener() {
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
