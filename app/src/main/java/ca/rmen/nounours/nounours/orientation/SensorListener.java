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

package ca.rmen.nounours.nounours.orientation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import ca.rmen.nounours.Constants;
import ca.rmen.nounours.NounoursRecorder;
import ca.rmen.nounours.compat.DisplayCompat;
import ca.rmen.nounours.data.Image;
import ca.rmen.nounours.data.Theme;
import ca.rmen.nounours.nounours.AndroidNounours;

/**
 * Manages shaking and tilting events for Nounours on the Android device.
 *
 * @author Carmen Alvarez
 */
public class SensorListener implements SensorEventListener {
    private static final String TAG = Constants.TAG + SensorListener.class.getSimpleName();

    private float mXAccel = Float.MAX_VALUE;
    private float mYAccel = Float.MAX_VALUE;
    private float mZAccel = Float.MAX_VALUE;
    private boolean mIsTiltImage = false;
    private final Set<OrientationImage> mOrientationImages = new HashSet<>();

    private AndroidNounours mNounours = null;
    private final Context mContext;

    private float[] mLastAcceleration = null;
    private float[] mLastMagneticField = null;

    public SensorListener(AndroidNounours nounours,
                          Context context) {
        mNounours = nounours;
        mContext = context;
        mLastMagneticField = new float[]{0, 0, -1};
        rereadOrientationFile(mNounours.getCurrentTheme(), context);
    }

    public void rereadOrientationFile(final Theme theme, final Context context) {
        Log.v(TAG, "rereadOrientationFile");
        mOrientationImages.clear();
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                InputStream orientationImageFile = getOrientationFile(theme, context);
                if (orientationImageFile != null) {
                    OrientationImageReader orientationImageReader;
                    try {
                        orientationImageReader = new OrientationImageReader(
                                orientationImageFile);
                        mOrientationImages.addAll(orientationImageReader
                                .getOrientationImages());
                    } catch (IOException e) {
                        Log.v(TAG, e.getMessage(), e);
                    }
                } else
                    Log.v(TAG, "No orientation file!");
                return null;
            }
        }.execute();

    }

    /**
     * Find the orientation file.
     *
     * @return a stream to read the orientation file for the given theme.
     */
    private InputStream getOrientationFile(Theme theme, Context context) {
        try {
            return context.getAssets().open("themes/" + theme.getId()+ "/orientationimage2.csv");
        } catch (IOException e) {
            Log.v(TAG, "Couldn't open orientation file: " + e.getMessage(), e);
        }

        return null;

    }

    /**
     * Listen for accelerometer events, to know if we should shake. Listen for
     * orientation events to know if we should show a tilt image.
     *
     * @see android.hardware.SensorEventListener#onSensorChanged(SensorEvent)
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        // Don't do anything if we're shaking.
        if (mNounours.isShaking() || mNounours.isLoading()) {
            mXAccel = Float.MAX_VALUE;
            mYAccel = Float.MAX_VALUE;
            mZAccel = Float.MAX_VALUE;
            return;
        }
        int sensorType = event.sensor.getType();

        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            onAccelerationChanged(event);
        } else if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) {
            onMagneticFieldChanged(event);
        }
        onOrientationChanged();
    }

    /**
     * Display a shake animation if the user shook the device.
     */
    private void onAccelerationChanged(SensorEvent event) {

        float[] values = event.values;
        float eventAccelX = values[0];
        float eventAccelY = values[1];
        float eventAccelZ = values[2];
        if (mXAccel != Float.MAX_VALUE) {
            final float netAccelX = Math.abs(mXAccel - eventAccelX);
            final float netAccelY = Math.abs(mYAccel - eventAccelY);
            final float netAccelZ = Math.abs(mZAccel - eventAccelZ);

            final float shakeFactor = mNounours.getMinShakeSpeed();
            if (netAccelX > shakeFactor || netAccelY > shakeFactor
                    || netAccelZ > shakeFactor) {
                mNounours.onShake();
            }
        }
        // For some reason, the first reading when the app starts, will give
        // some values of 0.0, which shouldn't be possible.
        if (eventAccelX != 0.0
                && eventAccelY != 0.0
                && eventAccelZ != 0.0) {
            mXAccel = eventAccelX;
            mYAccel = eventAccelY;
            mZAccel = eventAccelZ;
        }
        mLastAcceleration = values.clone();
    }

    /**
     * Display a special image if the device is in a given orientation.
     */
    private void onOrientationChanged() {
        float[] inR = new float[16];
        float[] I = new float[16];

        // We need to have recorded acceleration and magnetic field at least once.
        if (mLastAcceleration == null
                || mLastMagneticField == null
                || !SensorManager.getRotationMatrix(inR, I,
                mLastAcceleration, mLastMagneticField))
            return;

        float[] outR = remapCoordinateSystem(inR);
        float[] orientationValues = new float[3];
        orientationValues = SensorManager.getOrientation(outR,
                orientationValues);
        float[] orientationValuesDeg = new float[3];
        for (int i = 0; i < orientationValues.length; i++) {
            orientationValuesDeg[i] = (float) (orientationValues[i] * 180.0f / Math.PI);
        }

        final float yaw = orientationValuesDeg[0];
        final float pitch = orientationValuesDeg[1];
        final float roll = orientationValuesDeg[2];
        for (final OrientationImage orientationImage : mOrientationImages) {
            if (yaw >= orientationImage.minYaw
                    && yaw <= orientationImage.maxYaw
                    && pitch >= orientationImage.minPitch
                    && pitch <= orientationImage.maxPitch
                    && roll >= orientationImage.minRoll
                    && roll <= orientationImage.maxRoll) {
                final Image image = mNounours.getCurrentTheme().getImages().get(
                        orientationImage.imageId);
                mNounours.stopAnimation();
                mNounours.setImage(image);
                NounoursRecorder nounoursRecorder = mNounours.getNounoursRecorder();
                if (nounoursRecorder.isRecording()) nounoursRecorder.addImage(image);
                // Note that we are currently displaying a "tilt" image.
                mIsTiltImage = true;
                return;
            }
        }
        // Couldn't find any tilt image for this orientation, reset to the
        // default image if currently displaying a
        // tilt image
        if (mIsTiltImage) {
            mNounours.reset();
            mIsTiltImage = false;
        }
    }

    private void onMagneticFieldChanged(SensorEvent event) {
        mLastMagneticField = event.values.clone();
    }

    private float[] remapCoordinateSystem(float[] values) {
        int x = SensorManager.AXIS_X;
        int y = SensorManager.AXIS_Y;

        int rotation = DisplayCompat.getRotation(mContext);
        switch (rotation) {
            case Surface.ROTATION_90:
                //noinspection SuspiciousNameCombination
                x = SensorManager.AXIS_Y;
                //noinspection SuspiciousNameCombination
                y = SensorManager.AXIS_MINUS_X;

                break;
            case Surface.ROTATION_180:
                y = SensorManager.AXIS_MINUS_Y;
                x = SensorManager.AXIS_MINUS_X;

                break;
            case Surface.ROTATION_270:
                //noinspection SuspiciousNameCombination
                x = SensorManager.AXIS_MINUS_Y;
                //noinspection SuspiciousNameCombination
                y = SensorManager.AXIS_X;

                break;
            case Surface.ROTATION_0:
            default:
                break;
        }

        float[] result = new float[values.length];
        SensorManager.remapCoordinateSystem(values, x, y, result);
        return result;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }


}
