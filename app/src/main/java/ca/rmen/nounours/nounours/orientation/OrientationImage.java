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

    private String mImageId = null;
    private float mMinYaw = Float.MAX_VALUE;
    private float mMaxYaw = Float.MAX_VALUE;
    private float mMinPitch = Float.MAX_VALUE;
    private float mMaxPitch = Float.MAX_VALUE;
    private float mMinRoll = Float.MAX_VALUE;
    private float mMaxRoll = Float.MAX_VALUE;

    /**
     * @param imageId The image to display if the device is tilted in the given
     *                direction.
     */
    public OrientationImage(String imageId, float minYaw, float maxYaw, float minPitch, float maxPitch, float minRoll,
                            float maxRoll) {
        mImageId = imageId;
        mMinYaw = minYaw;
        mMaxYaw = maxYaw;
        mMinPitch = minPitch;
        mMaxPitch = maxPitch;
        mMinRoll = minRoll;
        mMaxRoll = maxRoll;
    }

    public String getImageId() {
        return mImageId;
    }

    public float getMinYaw() {
        return mMinYaw;
    }

    public float getMaxYaw() {
        return mMaxYaw;
    }

    public float getMinPitch() {
        return mMinPitch;
    }

    public float getMaxPitch() {
        return mMaxPitch;
    }

    public float getMinRoll() {
        return mMinRoll;
    }

    public float getMaxRoll() {
        return mMaxRoll;
    }

    public String toString() {
        return "Orientation: " + mImageId + ", Yaw between " + mMinYaw + " and " + mMaxYaw + ", pitch between " + mMinPitch
                + " and " + mMaxPitch + ", roll between " + mMinRoll + " and " + mMaxRoll;
    }
}
