/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours;

import java.util.Map;

import com.nullwire.trace.ExceptionHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import ca.rmen.nounours.data.Animation;
import ca.rmen.nounours.data.ImageSet;
import ca.rmen.nounours.util.FileUtil;

/**
 * Android activity class which delegates nounours-specific logic to the
 * {@link AndroidNounours} class.
 *
 * @author Carmen Alvarez
 *
 */
public class NounoursActivity extends Activity {

    private Toast toast = null;

    AndroidNounours nounours = null;
    private SensorManager sensorManager = null;
    private AndroidNounoursGestureDetector nounoursGestureDetector = null;
    private AndroidNounoursSensorListener sensorListener = null;
    private AndroidNounoursOnTouchListener onTouchListener = null;
    private boolean wasPaused = false;
    // private boolean useSimulator = false;

    private static final int MENU_ABOUT = 1000;
    private static final int MENU_ACTION = 1001;
    private static final int MENU_RANDOM = 1002;
    private static final int MENU_HELP = 1003;
    private static final int MENU_TOGGLE_SOUND = 1004;
    private static final int MENU_THEMES = 1005;
    private static final int MENU_DEFAULT_THEME = 1006;

    static final String URL_CRASH_REPORT = "http://r24591.ovh.net/crashreport/";

    /**
     * Initialize nounours (read the CSV data files, register as a listener for
     * touch events).
     *
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // crash report
        ExceptionHandler.register(this, URL_CRASH_REPORT);

        setContentView(R.layout.main);

        final ImageView imageView = (ImageView) findViewById(R.id.ImageView01);
        nounours = new AndroidNounours(this);
        nounoursGestureDetector = new AndroidNounoursGestureDetector(nounours);
        imageView.setOnTouchListener(onTouchListener);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        final GestureDetector gestureDetector = new GestureDetector(nounoursGestureDetector);
        onTouchListener = new AndroidNounoursOnTouchListener(nounours, this, gestureDetector);

        sensorListener = new AndroidNounoursSensorListener(nounours, this);
        imageView.setOnTouchListener(onTouchListener);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        /*
         * if (useSimulator) { Hardware.mContentResolver = getContentResolver();
         * sensorManager = new SensorManagerSimulator(sensorManager);
         * SensorManagerSimulator.connectSimulator(); }
         */
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, R.string.toast_remindMenuButton, Toast.LENGTH_LONG);
        toast.show();

    }

    /**
     * Called when the application is started or becomes active. Register for
     * sensor events, enable pinging for idle activity, and call
     * nounours.onResume().
     *
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {

        sensorManager.registerListener(sensorListener, SensorManager.SENSOR_ACCELEROMETER
                | SensorManager.SENSOR_ORIENTATION_RAW, SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
        if (wasPaused) {
            nounours.onResume();
        }
        nounours.doPing(true);
        wasPaused = false;

    }

    /**
     * The application was stopped or exited. Stop listening for sensor events,
     * stop pinging for idleness, and stop any sound.
     *
     * @see android.app.Activity#onStop()
     */
    @Override
    protected void onStop() {
        stopActivity();
        super.onStop();
    }

    /**
     * The application is paused. Stop listening for sensor events, stop pinging
     * for idleness, stop any sound.
     *
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        wasPaused = true;
        super.onPause();
        stopActivity();
    }

    /**
     * Stop listening for sensor events, stop pinging for idleness, stop any
     * sound.
     */
    private void stopActivity() {
        nounours.doPing(false);
        nounours.stopSound();
        sensorManager.unregisterListener(sensorListener);
    }

    /**
     * Create menu items for the different animations.
     *
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        int mainMenuIdx = 0;
        // Set up the actions menu
        final SubMenu actionMenu = menu.addSubMenu(Menu.NONE, MENU_ACTION, mainMenuIdx++, R.string.actions);
        actionMenu.setIcon(R.drawable.menu_action);
        int actionMenuIdx = 0;
        // Random action
        actionMenu.add(Menu.NONE, MENU_RANDOM, actionMenuIdx++, R.string.random);
        // All the animations
        final Map<String, Animation> animations = nounours.getAnimations();
        for (final Animation animation : animations.values()) {
            if (animation.isVisible()) {
                final int animationId = Integer.parseInt(animation.getId());
                actionMenu.add(Menu.NONE, animationId, actionMenuIdx++, getResources().getIdentifier(
                        animation.getLabel(), "string", getClass().getPackage().getName()));
            }

        }
        // Set up the toggle sound menu
        final MenuItem toggleSoundMenu = menu.add(Menu.NONE, MENU_TOGGLE_SOUND, mainMenuIdx++, R.string.disablesound);
        toggleSoundMenu.setIcon(R.drawable.ic_volume_off_small);

        if (FileUtil.isSdPresent()) {
            final SubMenu themesMenu = menu.addSubMenu(Menu.NONE, MENU_THEMES, mainMenuIdx++, R.string.themes);
            themesMenu.setIcon(R.drawable.ic_menu_gallery);

            final Map<String, ImageSet> imageSets = nounours.getImageSets();
            int imageSetIdx = 0;
            themesMenu.add(Menu.NONE, MENU_DEFAULT_THEME, imageSetIdx++, R.string.defaultTheme);
            for (ImageSet imageSet : imageSets.values()) {
                int imageSetId = Integer.parseInt(imageSet.getId());
                themesMenu.add(Menu.NONE, imageSetId, imageSetIdx++, getResources().getIdentifier(imageSet.getName(),
                        "string", getClass().getPackage().getName()));
            }
        }

        // Set up the help menu
        final MenuItem helpMenu = menu.add(Menu.NONE, MENU_HELP, mainMenuIdx++, R.string.help);
        helpMenu.setIcon(R.drawable.ic_menu_help);

        // Set up the about menu
        final MenuItem aboutMenu = menu.add(Menu.NONE, MENU_ABOUT, mainMenuIdx++, R.string.about);
        aboutMenu.setIcon(R.drawable.ic_menu_info_details);
        return true;
    }

    /**
     * Show a dialog box
     *
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(final int id) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // The user clicked on the about menu item.
        if (id == MENU_ABOUT) {
            builder.setTitle(R.string.about);
            builder.setIcon(R.drawable.ic_dialog_info);
            builder.setView(View.inflate(this, R.layout.layout_about, null));
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialog, final int whichButton) {
                    // nothing
                }
            });

        }
        return builder.create();
    }

    /**
     * Disable/enable any menu items.
     */
    public boolean onPrepareOptionsMenu(final Menu menu) {
        // Prevent changing the theme in the middle of the animation.
        MenuItem themesMenu = menu.findItem(MENU_THEMES);
        if (themesMenu != null) {
            boolean enableThemes = true;
            if(nounours.isAnimationRunning() || !FileUtil.isSdPresent())
                enableThemes = false;
            themesMenu.setEnabled(enableThemes);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Handle menu item selections.
     *
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        // Show the about dialog
        if (menuItem.getItemId() == MENU_ABOUT) {
            showDialog(MENU_ABOUT);
            return true;

        }
        // Show the help
        else if (menuItem.getItemId() == MENU_HELP) {
            nounours.onHelp();
            return true;
        }
        // Enable or disable sound
        else if (menuItem.getItemId() == MENU_TOGGLE_SOUND) {
            nounours.setEnableSound(!nounours.isSoundEnabled());
            if (nounours.isSoundEnabled()) {
                menuItem.setIcon(R.drawable.ic_volume_off_small);
                menuItem.setTitle(R.string.disablesound);
            } else {
                menuItem.setIcon(R.drawable.ic_volume_small);
                menuItem.setTitle(R.string.enablesound);
            }
            return true;

        }
        // The user picked the random animation
        else if (menuItem.getItemId() == MENU_RANDOM) {
            nounours.doRandomAnimation();
            return true;
        }
        // The user picked the default image theme
        else if (menuItem.getItemId() == MENU_DEFAULT_THEME) {

            nounours.useImageSet(Nounours.DEFAULT_THEME_ID);
            return true;
        }
        // Show an animation or change the theme.
        else {
            final Map<String, Animation> animations = nounours.getAnimations();
            final Map<String, ImageSet> imageSets = nounours.getImageSets();
            final Animation animation = animations.get("" + menuItem.getItemId());
            if (animation != null) {
                nounours.doAnimation(animation);
                return true;
            }
            final ImageSet imageSet = imageSets.get("" + menuItem.getItemId());
            if (imageSet != null) {
                nounours.useImageSet(imageSet.getId());
                return true;
            }
            return super.onOptionsItemSelected(menuItem);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        nounours.onDestroy();
        System.exit(0);
    }
}