/*
 *   Copyright (c) 2015 Carmen Alvarez
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
package ca.rmen.nounours.android.wear;

import ca.rmen.nounours.android.common.settings.NounoursSettings;

class WearSettings implements NounoursSettings {
    private final String mThemeId;
    private int mBackgroundColor;

    public WearSettings(String themeId) {
        mThemeId = themeId;
    }

    @Override
    public boolean isSoundEnabled() {
        return false;
    }

    @Override
    public void setEnableSound(boolean enabled) {
    }

    @Override
    public boolean isImageDimmed() {
        return false;
    }

    @Override
    public long getIdleTimeout() {
        return 90000;
    }

    @Override
    public String getThemeId() {
        return mThemeId;
    }

    public void setBackgroundColor(int backgroundColor) {
        mBackgroundColor = backgroundColor;
    }

    @Override
    public int getBackgroundColor(){
        return mBackgroundColor;
    }

}
