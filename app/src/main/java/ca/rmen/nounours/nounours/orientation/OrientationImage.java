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

/**
 * Represents an image to display if the device is set in the given orientation.
 *
 * @author Carmen Alvarez
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
     * @param imageId The image to display if the device is tilted in the given
     *                direction.
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
