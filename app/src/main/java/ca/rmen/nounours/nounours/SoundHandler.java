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

package ca.rmen.nounours.nounours;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ca.rmen.nounours.BuildConfig;
import ca.rmen.nounours.Constants;
import ca.rmen.nounours.Nounours;
import ca.rmen.nounours.NounoursSoundHandler;
import ca.rmen.nounours.data.Sound;
import ca.rmen.nounours.data.Theme;
import ca.rmen.nounours.util.FileUtil;

/**
 * Manages sound effects and music for Nounours on the Android device.
 *
 * @author Carmen Alvarez
 */
class SoundHandler implements NounoursSoundHandler, OnErrorListener {
    private static final String TAG = Constants.TAG + SoundHandler.class.getSimpleName();

    private final MediaPlayer mMediaPlayer;

    private final Nounours mNounours;
    private final Context mContext;


    public SoundHandler(Nounours nounours, Context context) {
        mNounours = nounours;
        mContext = context;
        // Initialize the media player.
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnErrorListener(this);
    }


    /**
     * For some reason, sounds will only play if they are on the sdcard. The
     * first time we try to play a sound, copy it first to the sdcard.
     *
     * @param sound one of Nounours' sounds.
     * @return The file for the given sound
     * @throws IOException
     */
    private File getSoundFile(final Sound sound) throws IOException {

        // Get the nounours directory on the sdcard
        if (!FileUtil.isSdPresent()) return null;
        final File appRootDirectory = mNounours.getAppDir();
        if (!appRootDirectory.isDirectory()) return null;

        // Check if the sound file exists already.
        Theme theme = mNounours.getCurrentTheme();
        File sdSoundFile;
        if (theme.getId().equals(Nounours.DEFAULT_THEME_ID))
            sdSoundFile = new File(appRootDirectory, sound.getFilename());
        else {
            sdSoundFile = new File(appRootDirectory + File.separator + theme.getId() + File.separator
                    + sound.getFilename());
        }
        if (sdSoundFile.exists()) {
            // See if the file needs to be replaced
            final String resourcePathStr;
            try {
                resourcePathStr = mContext.getPackageManager().getApplicationInfo(BuildConfig.APPLICATION_ID, 0).sourceDir;
                final File resourcePath = new File(resourcePathStr);
                if (resourcePath.lastModified() < sdSoundFile.lastModified()) {
                    Log.v(TAG, sound + " on sdcard is already up to date");
                    return sdSoundFile;
                }
                Log.v(TAG, "Need to update " + sound + " on sdcard");
            } catch (PackageManager.NameNotFoundException e) {
                Log.v(TAG, e.getMessage(), e);
            }
        } else {
            Log.v(TAG, "Need to create " + sound + " on sdcard");
        }

        // We need to create the sound file. Retrieve the sound file from the
        // raw resources.
        Log.v(TAG, "Looking for " + sdSoundFile);
        final InputStream soundFileData;
        if(theme.getId().equals(Nounours.DEFAULT_THEME_ID)) {
            final String resourceSoundFileName = sound.getFilename().substring(0, sound.getFilename().lastIndexOf('.'));
            final int soundResId = mContext.getResources().getIdentifier(resourceSoundFileName, "raw",
                    mContext.getClass().getPackage().getName());
            soundFileData = mContext.getResources().openRawResource(soundResId);
        } else {
            soundFileData = mContext.getAssets().open("themes/" + theme.getId() + "/" + sound.getFilename());
        }

        // Write the file
        final FileOutputStream writer = new FileOutputStream(sdSoundFile);
        FileUtil.copy(soundFileData, writer);
        // Return the newly created sdcard file.
        return sdSoundFile;
    }

    /**
     * Play a sound.
     */
    public void playSound(final String soundId) {
        Log.v(TAG, "playSound " + soundId);
        final Sound sound = mNounours.getSound(soundId);

        try {
            // Get the sound file from the sdcard.
            final File soundFile = getSoundFile(sound);
            // Prepare the media player
            mMediaPlayer.reset();
            if (soundFile != null && soundFile.exists()) {
                mMediaPlayer.setDataSource(soundFile.getAbsolutePath());
                mMediaPlayer.prepare();
            }

            // Play the sound.
            mMediaPlayer.start();
        } catch (final Exception e) {
            Log.v(TAG, "Error loading sound " + sound, e);
        }
    }

    /**
     * Stop playing a sound.
     *
     * @see ca.rmen.nounours.Nounours#stopSound()
     */
    public void stopSound() {
        mMediaPlayer.stop();
    }

    /**
     * Mute or unmute the media player.
     *
     * @see ca.rmen.nounours.Nounours#setEnableSound(boolean)
     */
    public void setEnableSound(final boolean enableSound) {
        if (enableSound) {
            mMediaPlayer.setVolume(1f, 1f);
        } else {
            mMediaPlayer.setVolume(0f, 0f);
        }
    }

    /**
     * Some error occurred using the media player
     *
     * @see android.media.MediaPlayer.OnErrorListener#onError(android.media.MediaPlayer,
     * int, int)
     */
    @Override
    public boolean onError(final MediaPlayer mp, final int what, final int extra) {
        Log.v(TAG, "MediaPlayer error: MediaPlayer = " + mp + "(" + mp.getClass() + "), what=" + what
                + ", extra = " + extra);
        return false;
    }

}
