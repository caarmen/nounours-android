/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours.data;

/**
 * Represents an image to display if the device is set in the given orientation.
 * 
 * @author Carmen Alvarez
 * 
 */
public class OrientationImage {

    private String imageId = null;
    private float minYaw = Float.MAX_VALUE;
    private float maxYaw = Float.MAX_VALUE;
    private float minPitch = Float.MAX_VALUE;
    private float maxPitch = Float.MAX_VALUE;
    private float minRoll = Float.MAX_VALUE;
    private float maxRoll = Float.MAX_VALUE;

    /**
     * @param imageId
     *            The image to display if the device is tilted in the given
     *            direction.
     * @param minYaw
     * @param maxYaw
     * @param minPitch
     * @param maxPitch
     * @param minRoll
     * @param maxRoll
     */
    public OrientationImage(String imageId, float minYaw, float maxYaw, float minPitch, float maxPitch, float minRoll,
            float maxRoll) {
        this.imageId = imageId;
        this.minYaw = minYaw;
        this.maxYaw = maxYaw;
        this.minPitch = minPitch;
        this.maxPitch = maxPitch;
        this.minRoll = minRoll;
        this.maxRoll = maxRoll;
    }

    public String getImageId() {
        return imageId;
    }

    public float getMinYaw() {
        return minYaw;
    }

    public float getMaxYaw() {
        return maxYaw;
    }

    public float getMinPitch() {
        return minPitch;
    }

    public float getMaxPitch() {
        return maxPitch;
    }

    public float getMinRoll() {
        return minRoll;
    }

    public float getMaxRoll() {
        return maxRoll;
    }

    public String toString() {
        return "Orientation: " + imageId + ", Yaw between " + minYaw + " and " + maxYaw + ", pitch between " + minPitch
                + " and " + maxPitch + ", roll between " + minRoll + " and " + maxRoll;
    }
}
