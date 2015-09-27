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

package ca.rmen.nounours.compat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

public class BitmapCompat {

    public static BitmapDrawable createBitmapDrawable(Context context, Bitmap bitmap) {
        if (ApiHelper.getAPILevel() >= 4) {
            return Api4Helper.createBitmapDrawable(context.getResources(), bitmap);
        }
        //noinspection deprecation
        return new BitmapDrawable(bitmap);

    }

    public static BitmapFactory.Options createBitmapFactoryOptions(int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        if (ApiHelper.getAPILevel() >= 4) {
            Api4Helper.setBitmapFactoryOptions(options);
        }
        return options;
    }
}
