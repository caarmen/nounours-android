/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours.nounours;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ca.rmen.nounours.BuildConfig;
import ca.rmen.nounours.Nounours;
import ca.rmen.nounours.NounoursSoundHandler;
import ca.rmen.nounours.data.Sound;
import ca.rmen.nounours.data.Theme;
import ca.rmen.nounours.util.FileUtil;
import ca.rmen.nounours.util.Trace;

/**
 * Manages sound effects and music for Nounours on the Android device.
 *
 * @author Carmen Alvarez
 */
class SoundHandler implements NounoursSoundHandler, OnErrorListener {
    private static final String APP_SD_DIR = "nounours";
    private MediaPlayer mediaPlayer = null;

    private Nounours nounours = null;

    public SoundHandler(Nounours nounours, Context context) {
        this.nounours = nounours;
        this.context = context;
        // Initialize the media player.
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnErrorListener(this);

    }

    private Context context = null;

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
        if (!FileUtil.isSdPresent())
            return null;
        final File externalStorageDirectory = Environment.getExternalStorageDirectory();
        final File appRootDirectory = new File(externalStorageDirectory, APP_SD_DIR);
        if (!appRootDirectory.exists() && !appRootDirectory.mkdir()) {
            return null;
        }

        // Check if the sound file exists already.
        Theme theme = nounours.getCurrentTheme();
        File sdSoundFile;
        if (theme.getId().equals(Nounours.DEFAULT_THEME_ID))
            sdSoundFile = new File(appRootDirectory, sound.getFilename());
        else {
            sdSoundFile = new File(appRootDirectory + File.separator + theme.getId() + File.separator
                    + sound.getFilename());
            return sdSoundFile;
        }
        if (sdSoundFile.exists()) {
            // See if the file needs to be replaced
            final String resourcePathStr;
            try {
                resourcePathStr = context.getPackageManager().getApplicationInfo(BuildConfig.APPLICATION_ID, 0).sourceDir;
                final File resourcePath = new File(resourcePathStr);
                if (resourcePath.lastModified() < sdSoundFile.lastModified()) {
                    Trace.debug(this, sound + " on sdcard is already up to date");
                    return sdSoundFile;
                }
                Trace.debug(this, "Need to update " + sound + " on sdcard");
            } catch (PackageManager.NameNotFoundException e) {
                Trace.debug(this, e);
            }
        } else {
            Trace.debug(this, "Need to create " + sound + " on sdcard");
        }

        // We need to create the sound file. Retrieve the sound file from the
        // raw resources.
        Trace.debug(this, "Looking for " + sdSoundFile);
        final String resourceSoundFileName = sound.getFilename().substring(0, sound.getFilename().lastIndexOf('.'));
        final int soundResId = context.getResources().getIdentifier(resourceSoundFileName, "raw",
                context.getClass().getPackage().getName());
        final InputStream soundFileData = context.getResources().openRawResource(soundResId);

        // Write the file
        final FileOutputStream writer = new FileOutputStream(sdSoundFile);
        FileUtil.copy(soundFileData, writer);
        final byte[] buffer = new byte[1024];
        for (int read = soundFileData.read(buffer, 0, buffer.length); read > 0; read = soundFileData.read(buffer, 0,
                buffer.length)) {
            writer.write(buffer, 0, read);
        }
        // Return the newly created sdcard file.
        return sdSoundFile;
    }

    /**
     * Play a sound.
     */
    public void playSound(final String soundId) {
        final Sound sound = nounours.getSound(soundId);

        try {
            // Get the sound file from the sdcard.
            final File soundFile = getSoundFile(sound);
            // Prepare the media player
            mediaPlayer.reset();
            if (soundFile != null && soundFile.exists()) {
                mediaPlayer.setDataSource(soundFile.getAbsolutePath());
                mediaPlayer.prepare();
            }

            // Play the sound.
            mediaPlayer.start();
        } catch (final Exception e) {
            Trace.debug(this, "Error loading sound " + sound + ": " + e);
            Trace.debug(this, e);
        }
    }

    /**
     * Stop playing a sound.
     *
     * @see ca.rmen.nounours.Nounours#stopSound()
     */
    public void stopSound() {
        mediaPlayer.stop();
    }

    /**
     * Mute or unmute the media player.
     *
     * @see ca.rmen.nounours.Nounours#setEnableSound(boolean)
     */
    public void setEnableSound(final boolean enableSound) {
        if (enableSound) {
            mediaPlayer.setVolume(1f, 1f);
        } else {
            mediaPlayer.setVolume(0f, 0f);
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
        Trace.debug(this, "MediaPlayer error: MediaPlayer = " + mp + "(" + mp.getClass() + "), what=" + what
                + ", extra = " + extra);
        return false;
    }

}
