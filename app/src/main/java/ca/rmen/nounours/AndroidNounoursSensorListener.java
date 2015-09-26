/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.view.Surface;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import ca.rmen.nounours.data.Image;
import ca.rmen.nounours.data.OrientationImage;
import ca.rmen.nounours.data.Theme;
import ca.rmen.nounours.io.OrientationImageReader;
import ca.rmen.nounours.util.DisplayCompat;
import ca.rmen.nounours.util.Trace;

/**
 * Manages shaking and tilting events for Nounours on the Android device.
 *
 * @author Carmen Alvarez
 */
class AndroidNounoursSensorListener implements SensorEventListener {
    private float xAccel = Float.MAX_VALUE;
    private float yAccel = Float.MAX_VALUE;
    private float zAccel = Float.MAX_VALUE;
    private boolean isTiltImage = false;
    private final Set<OrientationImage> orientationImages = new HashSet<>();

    private AndroidNounours nounours = null;

    private float[] lastAcceleration = null;
    private float[] lastMagneticField = null;

    public AndroidNounoursSensorListener(AndroidNounours nounours,
                                         Context context) {
        this.nounours = nounours;
        rereadOrientationFile(nounours.getCurrentTheme(), context);
        lastMagneticField = new float[]{0, 0, -1};
    }

    public void rereadOrientationFile(final Theme theme, final Context context) {
        Trace.debug(this, "rereadOrientationFile");
        orientationImages.clear();
        new AsyncTask<Void,Void,Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                InputStream orientationImageFile = getOrientationFile(theme, context);
                if (orientationImageFile != null) {
                    OrientationImageReader orientationImageReader;
                    try {
                        orientationImageReader = new OrientationImageReader(
                                orientationImageFile);
                        orientationImages.addAll(orientationImageReader
                                .getOrientationImages());
                    } catch (IOException e) {
                        Trace.debug(this, e);
                    }
                } else
                    Trace.debug(this, "No orientation file!");
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

        if (theme.getId().equals(Nounours.DEFAULT_THEME_ID)) {
            return context.getResources()
                    .openRawResource(R.raw.orientationimage);
        }
        String themesDir = nounours.getAppDir().getAbsolutePath();
        try {
            File orientationImageFile = new File(themesDir + File.separator
                    + theme.getId() + File.separator + "orientationimage2.csv");
            if (!orientationImageFile.exists()) {
                URI remoteOrientationImageFile = new URI(theme.getLocation()
                        + "/orientationimage2.csv");
                Util.downloadFile(remoteOrientationImageFile,
                        orientationImageFile);
                if (!orientationImageFile.exists())
                    return null;
                return new FileInputStream(orientationImageFile);
            }
            return new FileInputStream(orientationImageFile);
        } catch (IOException | URISyntaxException e) {
            Trace.debug(this, e);
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
        if (nounours.isShaking() || nounours.isLoading()) {
            xAccel = Float.MAX_VALUE;
            yAccel = Float.MAX_VALUE;
            zAccel = Float.MAX_VALUE;
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
        if (xAccel != Float.MAX_VALUE) {
            final float netAccelX = Math.abs(xAccel - eventAccelX);
            final float netAccelY = Math.abs(yAccel - eventAccelY);
            final float netAccelZ = Math.abs(zAccel - eventAccelZ);

            final float shakeFactor = nounours.getMinShakeSpeed();
            if (netAccelX > shakeFactor || netAccelY > shakeFactor
                    || netAccelZ > shakeFactor) {
                nounours.onShake();
            }
        }
        // For some reason, the first reading when the app starts, will give
        // some values of 0.0, which shouldn't be possible.
        if (eventAccelX != 0.0
                && eventAccelY != 0.0
                && eventAccelZ != 0.0) {
            xAccel = eventAccelX;
            yAccel = eventAccelY;
            zAccel = eventAccelZ;
        }
        lastAcceleration = values.clone();
    }

    /**
     * Display a special image if the device is in a given orientation.
     */
    private void onOrientationChanged() {
        float[] inR = new float[16];
        float[] I = new float[16];

        // We need to have recorded acceleration and magnetic field at least once.
        if (lastAcceleration == null
                || lastMagneticField == null
                || !SensorManager.getRotationMatrix(inR, I,
                lastAcceleration, lastMagneticField))
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
        for (final OrientationImage orientationImage : orientationImages) {
            if (yaw >= orientationImage.getMinYaw()
                    && yaw <= orientationImage.getMaxYaw()
                    && pitch >= orientationImage.getMinPitch()
                    && pitch <= orientationImage.getMaxPitch()
                    && roll >= orientationImage.getMinRoll()
                    && roll <= orientationImage.getMaxRoll()) {
                final Image image = nounours.getImages().get(
                        orientationImage.getImageId());
                nounours.stopAnimation();
                nounours.setImage(image);
                // Note that we are currently displaying a "tilt" image.
                isTiltImage = true;
                return;
            }
        }
        // Couldn't find any tilt image for this orientation, reset to the
        // default image if currently displaying a
        // tilt image
        if (isTiltImage) {
            nounours.reset();
            isTiltImage = false;
        }
    }

    private void onMagneticFieldChanged(SensorEvent event) {
        lastMagneticField = event.values.clone();
    }

    private float[] remapCoordinateSystem(float[] values) {
        int x = SensorManager.AXIS_X;
        int y = SensorManager.AXIS_Y;

        int rotation = DisplayCompat.getRotation(nounours.context);
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
