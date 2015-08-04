package ca.rmen.nounours.util;

import android.annotation.TargetApi;
import android.view.Display;

@TargetApi(8)
public class NounoursApi8Helper {
    public static int getRotation(Display display) {
        return display.getRotation();
    }
}
