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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.external.nbadal.AnimatedGifEncoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ca.rmen.nounours.Constants;
import ca.rmen.nounours.compat.EnvironmentCompat;
import ca.rmen.nounours.data.Animation;
import ca.rmen.nounours.data.AnimationImage;
import ca.rmen.nounours.nounours.cache.ImageCache;

public class AnimationUtil {
    private static final String TAG = Constants.TAG + AnimationUtil.class.getSimpleName();

    /**
     * Save an animation as an animated gif.
     *
     * @return a file containing the animated gif render of the given animation.
     */
    public static File saveAnimation(Context context, Animation animation) {
        Log.v(TAG, "saveAnimation " + animation);
        if (!FileUtil.isSdPresent()) return null;
        ImageCache imageCache = new ImageCache();
        try {

            //http://stackoverflow.com/questions/16331437/how-to-create-an-animated-gif-from-jpegs-in-android-development
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            AnimatedGifEncoder encoder = new AnimatedGifEncoder();
            encoder.start(bos);
            encoder.setRepeat(0);

            // Go through the list of images in the nounours animation, "repeat"
            // times.
            for (int i = 0; i < animation.getRepeat(); i++) {
                for (final AnimationImage animationImage : animation.getImages()) {
                    Bitmap bitmap = imageCache.getDrawableImage(context, animationImage.getImage());
                    int frameDuration = (int) (animation.getInterval() * animationImage.getDuration());
                    encoder.setDelay(frameDuration);
                    encoder.addFrame(bitmap);
                }
            }
            Log.v(TAG, "saveAnimation: finish writing gif...");
            encoder.finish();
            File file = new File(EnvironmentCompat.getExternalFilesDir(context), "nounours-animation.gif");
            Log.v(TAG, "Saving file " + file);
            FileOutputStream fos = new FileOutputStream(file);
            InputStream is = new ByteArrayInputStream(bos.toByteArray());
            FileUtil.copy(is, fos);
            Log.v(TAG, "Saved file " + file);
            return file;
        } catch (IOException | OutOfMemoryError e) {
            Log.w(TAG, "Couldn't write animated gif: " + e.getMessage(), e);
            return null;
        } finally {
            imageCache.clearImageCache();
        }
    }

    /**
     * Show the imageView and start its animation drawable.
     */
    public static void startAnimation(final ImageView imageView) {
        if (imageView.getVisibility() != View.VISIBLE) {
            Log.v(TAG, "startAnimation");
            imageView.setVisibility(View.VISIBLE);
            final AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getDrawable();
            // On some devices, directly calling start() on the animation does not work.
            // We have to wait until the ImageView is visible before starting the animation.
            imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation")
                @Override
                public void onGlobalLayout() {
                    if (!animationDrawable.isRunning()) {
                        imageView.post(new Runnable() {

                            @Override
                            public void run() {
                                animationDrawable.setVisible(true, false);
                                animationDrawable.start();
                            }
                        });
                    }
                    imageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });
        }
    }

    /**
     * Stop the animation drawable on this imageView and hide the imageView.
     */
    public static void stopAnimation(final ImageView imageView) {
        if (imageView.getVisibility() == View.VISIBLE) {
            Log.v(TAG, "stopAnimation");
            imageView.setVisibility(View.INVISIBLE);
            final AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getDrawable();
            animationDrawable.setVisible(false, false);
        }
    }

}
