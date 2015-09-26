/*
 * Copyright (c) 2015 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours.util;

import android.content.Context;

import ca.rmen.nounours.R;
import ca.rmen.nounours.data.Theme;

public final class ThemeUtil {

    private ThemeUtil() {
        // Prevent instantiation
    }

    public static CharSequence getThemeLabel(Context context, Theme theme) {
        String themeLabel = theme.getName();
        int themeLabelId = context.getResources().getIdentifier(theme.getName(), "string",
                R.class.getPackage().getName());
        if (themeLabelId > 0)
            return context.getResources().getText(themeLabelId);
        return themeLabel;
    }

}
