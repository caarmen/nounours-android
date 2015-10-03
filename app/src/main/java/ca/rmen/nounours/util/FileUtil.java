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

package ca.rmen.nounours.util;

import android.os.Environment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isSdPresent() {
        final String externalStorageState = Environment.getExternalStorageState();
        return externalStorageState.equals(Environment.MEDIA_MOUNTED)
                || externalStorageState.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
    }

    public static void copy(final InputStream in, final OutputStream out) throws IOException {
        try {
            final byte[] buffer = new byte[1500];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                out.flush();
            }
        } finally {
            in.close();
            out.close();
        }
    }
}
