/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours.compat;

import android.annotation.TargetApi;
import android.content.Context;
import android.view.Display;

import java.io.File;

@TargetApi(8)
public class Api8Helper {
    private Api8Helper() {
        // prevent instantiation
    }

    public static int getRotation(Display display) {
        return display.getRotation();
    }

    public static File getExternalFilesDir(Context context, String name){
        return context.getExternalFilesDir(name);
    }
}
