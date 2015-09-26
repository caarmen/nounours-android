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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ca.rmen.nounours.io.CSVReader;
import ca.rmen.nounours.io.NounoursReader;

/**
 * Reads a CSV file about which images to display when the device is in a given
 * orientation.
 *
 * @author Carmen Alvarez
 */
public class OrientationImageReader extends NounoursReader {

    private static final String COL_IMAGE_ID = "ImageId";
    private static final String COL_MIN_YAW = "MinYaw";
    private static final String COL_MAX_YAW = "MaxYaw";
    private static final String COL_MIN_PITCH = "MinPitch";
    private static final String COL_MAX_PITCH = "MaxPitch";
    private static final String COL_MIN_ROLL = "MinRoll";
    private static final String COL_MAX_ROLL = "MaxRoll";

    private final Set<OrientationImage> mOrientationImages = new HashSet<>();

    /**
     * Immediately reads the CSV content and caches the image-orientation data.
     * Required columns are: <code>
     * - ImageId: String. The id of the image to display if the device is tilted
     * in the given direction.
     * - MinYaw: integer between 0 and 360. Minimum yaw (corresponds to compass direction
     * with 0=North, 90=East).
     * - MaxYaw: integer between 0 and 360. Maximum yaw.
     * - MinPitch: integer between -180 and 180. Minimum pitch (0=phone laying flat,
     * -90= phone upright).
     * - MaxPitch: integer between -180 and 180.
     * - MinRoll: integer between -180 and 180.  Minimum roll (0=phone laying flat, 90=phone
     * tilted to the right)
     * - MaxRoll: integer between -180 and 180.  Maximum roll.
     * </code>
     *
     * @throws IOException
     */
    public OrientationImageReader(InputStream is) throws IOException {
        super(is);
        load();
    }

    /**
     * Read a line the CSV file, create an OrientationImage object, and add it
     * to the cache.
     */
    @Override
    protected void readLine(CSVReader reader) {
        String imageId = reader.getValue(COL_IMAGE_ID);
        float minYaw = Float.parseFloat(reader.getValue(COL_MIN_YAW));
        float maxYaw = Float.parseFloat(reader.getValue(COL_MAX_YAW));
        float minPitch = Float.parseFloat(reader.getValue(COL_MIN_PITCH));
        float maxPitch = Float.parseFloat(reader.getValue(COL_MAX_PITCH));
        float minRoll = Float.parseFloat(reader.getValue(COL_MIN_ROLL));
        float maxRoll = Float.parseFloat(reader.getValue(COL_MAX_ROLL));
        OrientationImage orientationImage = new OrientationImage(imageId, minYaw, maxYaw, minPitch, maxPitch, minRoll,
                maxRoll);
        mOrientationImages.add(orientationImage);
    }

    /**
     * @return the set of OrientationImage objects read from the CSV file.
     */
    public Set<OrientationImage> getOrientationImages() {
        return Collections.unmodifiableSet(mOrientationImages);
    }

}
