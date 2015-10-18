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
package ca.rmen.nounours.lwp;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import ca.rmen.nounours.compat.DisplayCompat;
import ca.rmen.nounours.nounours.AndroidNounours;
import ca.rmen.nounours.nounours.FlingDetector;
import ca.rmen.nounours.nounours.TouchListener;
import ca.rmen.nounours.nounours.orientation.SensorListener;

@TargetApi(Build.VERSION_CODES.ECLAIR_MR1)
public class LWPService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new NounoursLWPEngine();
    }

    class NounoursLWPEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {

        private boolean mWasPaused = false;
        private AndroidNounours mNounours = null;
        private SensorListener mSensorListener;
        private SensorManager mSensorManager;
        private Sensor mAccelerometerSensor;
        private Sensor mMagneticFieldSensor;
        private TouchListener mTouchListener;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            setTouchEventsEnabled(true);
            mNounours = new AndroidNounours(getBaseContext(), new Handler(), getSurfaceHolder(),
                    DisplayCompat.getWidth(getApplicationContext()),
                    DisplayCompat.getHeight(getApplicationContext()), mListener);
            FlingDetector nounoursFlingDetector = new FlingDetector(mNounours);
            final GestureDetector gestureDetector = new GestureDetector(getApplicationContext(), nounoursFlingDetector);
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            mTouchListener = new TouchListener(mNounours, gestureDetector);
            mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mMagneticFieldSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            mSensorListener = new SensorListener(mNounours, getApplicationContext());
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LWPService.this);
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                mSensorManager.registerListener(mSensorListener, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
                mSensorManager.registerListener(mSensorListener, mMagneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);
                mNounours.reloadSettings();
                if (mWasPaused) {
                    mNounours.onResume();
                }
                mNounours.doPing(true);
                mWasPaused = false;

            } else {
                mSensorManager.unregisterListener(mSensorListener);
                mWasPaused = true;
                mNounours.doPing(false);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            mNounours.redraw();
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xStep, float yStep, int xPixels, int yPixels) {
            mNounours.redraw();
        }

        /*
         * Store the position of the touch event so we can use it for drawing
         * later
         */
        @Override
        public void onTouchEvent(MotionEvent event) {
            mTouchListener.onTouch(null, event);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (mNounours != null) mNounours.reloadSettings();

        }

        private final AndroidNounours.AndroidNounoursListener mListener = new AndroidNounours.AndroidNounoursListener() {
            @Override
            public void onThemeLoadStart(int max, String message) {
            }

            @Override
            public void onThemeLoadProgress(int progress, int max, String message) {
            }

            @Override
            public void onThemeLoadComplete() {
                mSensorListener.rereadOrientationFile(getApplicationContext());
            }
        };

    }

}
