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

package ca.rmen.nounours.android.common.util;

import android.content.Context;

import ca.rmen.nounours.data.Theme;
import ca.rmen.nounours.common.R;

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

    public static boolean isThemeTransparent(Context context, String themeId) {
        String[] transparentThemes = context.getResources().getStringArray(R.array.transparentThemes);
        for(String transparentTheme : transparentThemes) {
            if(themeId.equals(transparentTheme)) return true;
        }
        return false;
    }

}
