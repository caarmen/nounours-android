/*
This source is part of the
     _____  ___   ____
 __ / / _ \/ _ | / __/___  _______ _
/ // / , _/ __ |/ _/_/ _ \/ __/ _ `/
\___/_/|_/_/ |_/_/ (_)___/_/  \_, /
                             /___/
repository. It is licensed under a Creative Commons
Attribution-Noncommercial-Share Alike 3.0 Unported License:
http://creativecommons.org/licenses/by-nc-sa/3.0.
Contact BoD@JRAF.org for more information.

$Id: FileUtil.java 625 2009-04-26 22:45:17Z bod $
*/
package ca.rmen.nounours.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;

public class FileUtil {
    public static boolean isSdPresent() {
        final String externalStorageState = Environment.getExternalStorageState();
        return externalStorageState.contains("mounted");
    }

    public static long copy(final InputStream in, final OutputStream out) throws IOException {
        long res = 0;
        final byte[] buffer = new byte[1500];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
            out.flush();
            res += read;
        }
        return res;
    }
}
