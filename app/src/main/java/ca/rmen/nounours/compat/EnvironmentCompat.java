/*
 * Copyright (c) 2015 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours.compat;

import android.content.Context;
import android.os.Environment;

import java.io.File;

import ca.rmen.nounours.util.FileUtil;
import ca.rmen.nounours.util.Trace;

public class EnvironmentCompat {

    private EnvironmentCompat() {
        // Prevent instantiation
    }
    public static File getExternalFilesDir(Context context) {
        File result;
        String folderName = "nounours";
        if (ApiHelper.getAPILevel() >= 8) {
            result = Api8Helper.getExternalFilesDir(context, folderName);
        } else {
            File sdcard = Environment.getExternalStorageDirectory();
            result = new File(sdcard, folderName);
        }
        if (!result.exists()) {
            if(!result.mkdirs() || !result.isDirectory()) {
                Trace.debug(FileUtil.class, "Could not create folder " + result);
            }
        }
        return result;
    }
}
