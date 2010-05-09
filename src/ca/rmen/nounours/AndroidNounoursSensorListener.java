/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours;

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
import ca.rmen.nounours.util.Trace;
import android.app.Activity;
import android.hardware.SensorListener;
import android.hardware.SensorManager;

/**
 * Manages shaking and tilting events for Nounours on the Android device.
 * 
 * @author Carmen Alvarez
 * 
 */
public class AndroidNounoursSensorListener implements SensorListener {
    private float xAccel = Float.MAX_VALUE;
    private float yAccel = Float.MAX_VALUE;
    private float zAccel = Float.MAX_VALUE;
    private boolean isTiltImage = false;
    private Set<OrientationImage> orientationImages = new HashSet<OrientationImage>();;

    private Nounours nounours = null;

    public AndroidNounoursSensorListener(Nounours nounours, Activity activity) {
        this.nounours = nounours;
        rereadOrientationFile(nounours.getCurrentTheme(), activity);
    }

    public void rereadOrientationFile(Theme theme, Activity activity) {
        Trace.debug(this, "rereadOrientationFile");
        orientationImages.clear();
        InputStream orientationImageFile = getOrientationFile(theme, activity);
        if (orientationImageFile != null) {
            OrientationImageReader orientationImageReader;
            try {
                orientationImageReader = new OrientationImageReader(orientationImageFile);
                orientationImages.addAll(orientationImageReader.getOrentationImages());
            } catch (IOException e) {
                Trace.debug(this, e);
            }
        }
        else
            Trace.debug(this,"No orientation file!");

    }

    /**
     * Find the orientation file.
     * 
     * @param activity
     * @return
     */
    private InputStream getOrientationFile(Theme theme, Activity activity) {

        if (theme.getId().equals(Nounours.DEFAULT_THEME_ID)) {
            InputStream orientationImageFile = activity.getResources().openRawResource(R.raw.orientationimage);
            return orientationImageFile;
        }
        String themesDir = nounours.getProperty(Nounours.PROP_DOWNLOADED_IMAGES_DIR);
        try {
            File orientationImageFile = new File(themesDir + File.separator + theme.getId() + File.separator
                    + "orientationimage.csv");
            if (!orientationImageFile.exists()) {
                URI remoteOrientationImageFile = new URI(theme.getLocation() + "/orientationimage.csv");
                Util.downloadFile(remoteOrientationImageFile, orientationImageFile);
                if (!orientationImageFile.exists())
                    return null;
                return new FileInputStream(orientationImageFile);
            }
            return new FileInputStream(orientationImageFile);
        } catch (IOException e) {
            Trace.debug(this, e);
        } catch (URISyntaxException e) {
            Trace.debug(this, e);
        }
        return null;

    }

    /**
     * Listen for accelerometer events, to know if we should shake. Listen for
     * orientation events to know if we should show a tilt image.
     * 
     * @see android.hardware.SensorListener#onSensorChanged(int, float[])
     */
    @Override
    public void onSensorChanged(final int sensor, final float[] values) {

        // Don't do anything if we're shaking.
        if (nounours.isShaking() || nounours.isLoading()) {
            xAccel = Float.MAX_VALUE;
            yAccel = Float.MAX_VALUE;
            zAccel = Float.MAX_VALUE;
            return;
        }
        // Display a shake animation if the user shook the device.
        if (sensor == SensorManager.SENSOR_ACCELEROMETER) {

            if (xAccel != Float.MAX_VALUE) {
                final float netAccelX = Math.abs(xAccel - values[SensorManager.DATA_X]);
                final float netAccelY = Math.abs(yAccel - values[SensorManager.DATA_Y]);
                final float netAccelZ = Math.abs(zAccel - values[SensorManager.DATA_Z]);

                final float shakeFactor = nounours.getMinShakeSpeed();
                if (netAccelX > shakeFactor || netAccelY > shakeFactor || netAccelZ > shakeFactor) {
                    nounours.onShake();
                }
            }
            // For some reason, the first reading when the app starts, will give
            // some values of 0.0, which shouldn't be possible.
            if (values[SensorManager.DATA_X] != 0.0 && values[SensorManager.DATA_Y] != 0.0
                    && values[SensorManager.DATA_Z] != 0.0) {
                xAccel = values[SensorManager.DATA_X];
                yAccel = values[SensorManager.DATA_Y];
                zAccel = values[SensorManager.DATA_Z];
            }
        }
        // Display a special image if the device is in a given orientation.
        else if (sensor == SensorManager.SENSOR_ORIENTATION_RAW) {
            final int offsetIndex = values.length == 3 ? 0 : SensorManager.RAW_DATA_INDEX;
            final float yaw = values[offsetIndex + SensorManager.DATA_X];
            final float pitch = values[offsetIndex + SensorManager.DATA_Y];
            final float roll = values[offsetIndex + SensorManager.DATA_Z];
            for (final OrientationImage orientationImage : orientationImages) {
                if (yaw >= orientationImage.getMinYaw() && yaw <= orientationImage.getMaxYaw()
                        && pitch >= orientationImage.getMinPitch() && pitch <= orientationImage.getMaxPitch()
                        && roll >= orientationImage.getMinRoll() && roll <= orientationImage.getMaxRoll()) {
                    final Image image = nounours.getImages().get(orientationImage.getImageId());
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

    }

    @Override
    public void onAccuracyChanged(int sensor, int accuracy) {
        // Do nothing
    }

}
