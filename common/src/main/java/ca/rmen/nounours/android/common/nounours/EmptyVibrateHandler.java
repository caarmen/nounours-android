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

package ca.rmen.nounours.android.common.nounours;

import ca.rmen.nounours.NounoursVibrateHandler;

/**
 * Manages vibration for Nounours on the android device.
 *
 * @author Carmen Alvarez
 */
public class EmptyVibrateHandler implements NounoursVibrateHandler {

    public EmptyVibrateHandler() {
    }

    @Override
    public void doVibrate(final long duration) {
    }

    @Override
    public void doVibrate(final long duration, final long interval) {
    }

}
