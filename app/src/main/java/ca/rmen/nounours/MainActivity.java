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
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Map;

import ca.rmen.nounours.compat.ApiHelper;
import ca.rmen.nounours.data.Animation;
import ca.rmen.nounours.data.Theme;
import ca.rmen.nounours.nounours.AndroidNounours;
import ca.rmen.nounours.nounours.FlingDetector;
import ca.rmen.nounours.nounours.TouchListener;
import ca.rmen.nounours.nounours.orientation.SensorListener;
import ca.rmen.nounours.settings.NounoursSettings;
import ca.rmen.nounours.settings.SettingsActivity;

/**
 * Android activity class which delegates mNounours-specific logic to the
 * {@link AndroidNounours} class.
 *
 * @author Carmen Alvarez
 */
public class MainActivity extends Activity {

    private static final String TAG = Constants.TAG + MainActivity.class.getSimpleName();

    private AndroidNounours mNounours;
    private SensorManager mSensorManager;
    private SensorListener mSensorListener;
    private TouchListener mTouchListener;
    private Sensor mAccelerometerSensor;
    private Sensor mMagneticFieldSensor;

    /**
     * Initialize mNounours (read the CSV data files, register as a listener for
     * touch events).
     *
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean useSimulator = Build.DEVICE.startsWith("generic") && ApiHelper.getAPILevel() == 3;

        setContentView(R.layout.main);

        final ImageView imageView = (ImageView) findViewById(R.id.ImageView01);
        mNounours = new AndroidNounours(this, new Handler(), imageView);

        FlingDetector nounoursFlingDetector = new FlingDetector(mNounours);
        imageView.setOnTouchListener(mTouchListener);
        //noinspection ConstantConditions
        if (!useSimulator) {
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        }
        if (mSensorManager != null) {
            mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mMagneticFieldSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }

        final GestureDetector gestureDetector = new GestureDetector(this, nounoursFlingDetector);
        mTouchListener = new TouchListener(mNounours, gestureDetector);
        mSensorListener = new SensorListener(mNounours, this);
        imageView.setOnTouchListener(mTouchListener);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        /*
         * if (useSimulator) { Hardware.mContentResolver = getContentResolver();
         * mSensorManager = new SensorManagerSimulator(mSensorManager);
         * SensorManagerSimulator.connectSimulator(); }
         */

        Toast.makeText(this, R.string.toast_remindMenuButton, Toast.LENGTH_LONG).show();

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

        super.onResume();
        if (mSensorManager != null) {
            mSensorManager.registerListener(mSensorListener, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            if (!mSensorManager.registerListener(mSensorListener, mMagneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL))
                Log.v(TAG, "Could not register for magnetic field sensor");
        }

        mNounours.setEnableSound(NounoursSettings.isSoundEnabled(this));
        mNounours.setEnableVibrate(NounoursSettings.isSoundEnabled(this));
        mNounours.setIdleTimeout(NounoursSettings.getIdleTimeout(this));
        mNounours.setEnableRandomAnimations(NounoursSettings.isRandomAnimationEnabled(this));
        boolean themeChanged = reloadThemeFromPreference();
        if(!themeChanged) mNounours.onResume();
        mNounours.doPing(true);

    }

    /**
     * The application was stopped or exited. Stop listening for sensor events,
     * stop pinging for idleness, and stop any sound.
     *
     * @see android.app.Activity#onStop()
     */
    @Override
    protected void onStop() {
        stopActivity();
        super.onStop();
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
        stopActivity();
    }

    /**
     * Stop listening for sensor events, stop pinging for idleness, stop any
     * sound.
     */
    private void stopActivity() {
        mNounours.doPing(false);
        mNounours.stopSound();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mSensorListener);
        }
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
                        .getIdentifier(animationLabel, "string", getClass().getPackage().getName());
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
        Theme theme = mNounours.getCurrentTheme();
        boolean nounoursIsBusy = mNounours.isAnimationRunning() || mNounours.isLoading();
        MenuItem animationMenu = menu.findItem(R.id.menu_animation);
        if (animationMenu != null) {
            animationMenu.setEnabled(!nounoursIsBusy);
            if (theme == null || theme.getAnimations().size() == 0)
                animationMenu.setVisible(false);
            else
                animationMenu.setVisible(true);
            setupAnimationMenu(animationMenu.getSubMenu());
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
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
        super.onDestroy();
        mNounours.onDestroy();
        System.exit(0);
    }

    private boolean reloadThemeFromPreference() {
        boolean nounoursIsBusy = mNounours.isLoading();
        Log.v(TAG, "reloadThemeFromPreference, nounoursIsBusy = " + nounoursIsBusy);
        String themeId = NounoursSettings.getThemeId(this);
        if(mNounours.getCurrentTheme() != null
                && mNounours.getCurrentTheme().getId().equals(themeId)) {
            return false;
        }
        final Theme theme;
        if(AndroidNounours.DEFAULT_THEME_ID.equals(themeId)) {
            theme = mNounours.getDefaultTheme();
        } else {
            final Map<String, Theme> themes = mNounours.getThemes();
            theme = themes.get(themeId);
        }
        if (theme != null) {
            mNounours.stopAnimation();
            final ImageView imageView = (ImageView) findViewById(R.id.ImageView01);
            imageView.setImageBitmap(null);
            mNounours.useTheme(theme.getId());
            mSensorListener.rereadOrientationFile(theme, MainActivity.this);
        }
        return true;
    }

}
