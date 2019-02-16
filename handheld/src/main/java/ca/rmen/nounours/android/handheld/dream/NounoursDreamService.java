/*
 *   Copyright (c) 2016 Carmen Alvarez
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
package ca.rmen.nounours.android.handheld.dream;


import android.annotation.TargetApi;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.service.dreams.DreamService;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceView;

import ca.rmen.nounours.R;
import ca.rmen.nounours.android.common.nounours.AndroidNounours;
import ca.rmen.nounours.android.common.nounours.EmptySoundHandler;
import ca.rmen.nounours.android.common.nounours.EmptyVibrateHandler;
import ca.rmen.nounours.android.common.nounours.NounoursRenderer;
import ca.rmen.nounours.android.common.nounours.ThemeLoadListener;
import ca.rmen.nounours.android.common.nounours.cache.ImageCache;
import ca.rmen.nounours.android.common.nounours.cache.NounoursResourceCache;
import ca.rmen.nounours.android.common.settings.NounoursSettings;
import ca.rmen.nounours.android.handheld.nounours.FlingDetector;
import ca.rmen.nounours.android.handheld.nounours.TouchListener;
import ca.rmen.nounours.android.handheld.nounours.orientation.SensorListener;
import ca.rmen.nounours.android.handheld.settings.SharedPreferenceSettings;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class NounoursDreamService extends DreamService {
    private AndroidNounours mNounours;
    private SensorListener mSensorListener;
    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    private Sensor mMagneticFieldSensor;
    private TouchListener mTouchListener;

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(true);
        setFullscreen(true);
        setContentView(R.layout.dream);
        SurfaceView surfaceView = findViewById(R.id.surface_view);
        NounoursSettings settings = SharedPreferenceSettings.getDreamSettings(this);
        settings.setEnableSound(false);
        mNounours = new AndroidNounours("DREAM",
                this,
                new Handler(),
                settings,
                surfaceView.getHolder(),
                new NounoursRenderer(),
                new NounoursResourceCache(this, settings, new ImageCache()),
                new EmptySoundHandler(),
                new EmptyVibrateHandler(),
                mListener);
        FlingDetector nounoursFlingDetector = new FlingDetector(mNounours);
        final GestureDetector gestureDetector = new GestureDetector(this, nounoursFlingDetector);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticFieldSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorListener = new SensorListener(mNounours, this);
        mTouchListener = new TouchListener(mNounours, gestureDetector);
    }

    @Override
    public void onDetachedFromWindow() {
        mNounours.onDestroy();
        super.onDetachedFromWindow();
    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();
        if(!mNounours.isLoading()) {
            if (mSensorManager != null) {
                mSensorManager.registerListener(mSensorListener, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
                mSensorManager.registerListener(mSensorListener, mMagneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
        mNounours.reloadSettings();
        mNounours.doPing(true);
    }

    @Override
    public void onDreamingStopped() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mSensorListener);
        }
        mNounours.doPing(false);
        super.onDreamingStopped();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        mTouchListener.onTouch(null, event);
        super.dispatchTouchEvent(event);
        return false;
    }

    private final ThemeLoadListener mListener = new ThemeLoadListener() {
        @Override
        public void onThemeLoadStart(int max, String message) {
        }

        @Override
        public void onThemeLoadProgress(int progress, int max, String message) {
        }

        @Override
        public void onThemeLoadComplete() {
            if (mSensorManager != null) {
                mSensorListener.rereadOrientationFile(getApplicationContext());
                mSensorManager.registerListener(mSensorListener, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
                mSensorManager.registerListener(mSensorListener, mMagneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    };
}
