/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Map;

import ca.rmen.nounours.data.Animation;
import ca.rmen.nounours.data.Theme;
import ca.rmen.nounours.util.Trace;

/**
 * Android activity class which delegates nounours-specific logic to the
 * {@link AndroidNounours} class.
 *
 * @author Carmen Alvarez
 */
public class NounoursActivity extends Activity {

    private Toast toast = null;

    private AndroidNounours nounours = null;
    private SensorManager sensorManager = null;
    private AndroidNounoursSensorListener sensorListener = null;
    private AndroidNounoursOnTouchListener onTouchListener = null;
    private Sensor accelerometerSensor = null;
    private Sensor magneticFieldSensor = null;

    private boolean wasPaused = false;

    private static final int MENU_ACTION = 1001;
    private static final int MENU_RANDOM = 1002;
    private static final int MENU_HELP = 1003;
    private static final int MENU_OPTIONS = 1011;

    /**
     * Initialize nounours (read the CSV data files, register as a listener for
     * touch events).
     *
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean useSimulator = true;

        setContentView(R.layout.main);

        final ImageView imageView = (ImageView) findViewById(R.id.ImageView01);
        nounours = new AndroidNounours(this, imageView);

        AndroidNounoursGestureDetector nounoursGestureDetector = new AndroidNounoursGestureDetector(nounours);
        imageView.setOnTouchListener(onTouchListener);
        //noinspection ConstantConditions
        if (!useSimulator) {
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        }
        if (sensorManager != null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }

        final GestureDetector gestureDetector = new GestureDetector(this, nounoursGestureDetector);
        onTouchListener = new AndroidNounoursOnTouchListener(nounours, gestureDetector);
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
     * sensor events, enable pinging for idle context, and call
     * nounours.onResume().
     *
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {

        if (sensorManager != null) {
            sensorManager.registerListener(sensorListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            if (!sensorManager.registerListener(sensorListener, magneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL))
                Trace.debug(this, "Could not register for magnetic field sensor");
        }

        nounours.setEnableSound(AndroidNounoursSettings.isSoundEnabled(this));
        nounours.setEnableVibrate(AndroidNounoursSettings.isSoundEnabled(this));
        nounours.setIdleTimeout(AndroidNounoursSettings.getIdleTimeout(this));
        nounours.setEnableRandomAnimations(AndroidNounoursSettings.isRandomAnimationEnabled(this));
        super.onResume();
        if (wasPaused) {
            nounours.onResume();
        }
        nounours.doPing(true);
        wasPaused = false;
        reloadThemeFromPreference();

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
        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorListener);
        }
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
        setupAnimationMenu(actionMenu);

        final SubMenu optionsMenu = menu.addSubMenu(Menu.NONE, MENU_OPTIONS, mainMenuIdx++, R.string.options);
        optionsMenu.setIcon(R.drawable.ic_menu_preferences);

        // Set up the help menu
        final MenuItem helpMenu = menu.add(Menu.NONE, MENU_HELP, mainMenuIdx, R.string.help);
        helpMenu.setIcon(R.drawable.ic_menu_help);

        return true;
    }

    private void setupAnimationMenu(SubMenu actionMenu) {
        int actionMenuIdx = 0;
        actionMenu.clear();
        actionMenu.add(Menu.NONE, MENU_RANDOM, actionMenuIdx++, R.string.random);
        // All the animations
        final Map<String, Animation> animations = nounours.getAnimations();
        for (final Animation animation : animations.values()) {
            if (animation.isVisible()) {
                final int animationId = Integer.parseInt(animation.getId());
                String animationLabel = animation.getLabel();
                int labelIdx = getResources()
                        .getIdentifier(animationLabel, "string", getClass().getPackage().getName());
                if (labelIdx > 0)
                    actionMenu.add(Menu.NONE, animationId, actionMenuIdx++, labelIdx);
                else
                    actionMenu.add(Menu.NONE, animationId, actionMenuIdx++, animationLabel);
            }

        }

    }

    /**
     * Disable/enable any menu items.
     */
    public boolean onPrepareOptionsMenu(final Menu menu) {
        // Prevent changing the theme in the middle of the animation.
        Theme theme = nounours.getCurrentTheme();
        boolean nounoursIsBusy = nounours.isAnimationRunning() || nounours.isLoading();
        MenuItem animationMenu = menu.findItem(MENU_ACTION);
        if (animationMenu != null) {
            animationMenu.setEnabled(!nounoursIsBusy);
            if (theme == null || theme.getAnimations().size() == 0)
                animationMenu.setVisible(false);
            else
                animationMenu.setVisible(true);
            setupAnimationMenu(animationMenu.getSubMenu());
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
        if (menuItem.getItemId() == MENU_OPTIONS) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        // Show the help
        else if (menuItem.getItemId() == MENU_HELP) {
            nounours.onHelp();
            return true;
        }
        // The user picked the random animation
        else if (menuItem.getItemId() == MENU_RANDOM) {
            nounours.doRandomAnimation();
            return true;
        }
        // Show an animation or change the theme.
        else {
            final Map<String, Animation> animations = nounours.getAnimations();
            final Animation animation = animations.get("" + menuItem.getItemId());
            if (animation != null) {
                nounours.doAnimation(animation);
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

    private void reloadThemeFromPreference() {
        boolean nounoursIsBusy = nounours.isAnimationRunning() || nounours.isLoading();
        Trace.debug(this, "reloadThemeFromPreference, nounoursIsBusy = " + nounoursIsBusy);
        String themeId = PreferenceManager.getDefaultSharedPreferences(this).getString(AndroidNounours.PREF_THEME, AndroidNounours.DEFAULT_THEME_ID);
        if(nounours.getCurrentTheme() != null
                && nounours.getCurrentTheme().getId().equals(themeId)) {
            return;
        }
        final Theme theme;
        if(AndroidNounours.DEFAULT_THEME_ID.equals(themeId)) {
            theme = nounours.getDefaultTheme();
        } else {
            final Map<String, Theme> themes = nounours.getThemes();
            theme = themes.get(themeId);
        }
        if (theme != null) {
            nounours.stopAnimation();
            nounours.useTheme(theme.getId());
            sensorListener.rereadOrientationFile(theme, NounoursActivity.this);
        }

    }

}
