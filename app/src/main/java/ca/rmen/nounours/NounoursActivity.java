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
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ca.rmen.nounours.data.Animation;
import ca.rmen.nounours.data.Theme;
import ca.rmen.nounours.io.ThemeReader;
import ca.rmen.nounours.io.ThemeUpdateListener;
import ca.rmen.nounours.util.FileUtil;
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
    private static final int MENU_THEMES = 1005;
    private static final int MENU_DEFAULT_THEME = 1006;
    private static final int MENU_UPDATES = 1007;
    private static final int MENU_LOAD_MORE_THEMES = 1008;
    private static final int MENU_UPDATE_THEME = 1009;
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

        boolean useSimulator = false;

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

        if (FileUtil.isSdPresent()) {
            final SubMenu themesMenu = menu.addSubMenu(Menu.NONE, MENU_THEMES, mainMenuIdx++, R.string.themes);
            themesMenu.setIcon(R.drawable.ic_menu_gallery);

            final Map<String, Theme> imageSets = nounours.getThemes();
            int imageSetIdx = 0;
            Theme curTheme = nounours.getCurrentTheme();
            String curThemeId = (curTheme == null) ? null : curTheme.getId();

            MenuItem themeMenuItem = themesMenu
                    .add(Menu.NONE, MENU_DEFAULT_THEME, imageSetIdx++, R.string.defaultTheme);
            if (Nounours.DEFAULT_THEME_ID.equals(curThemeId))
                themeMenuItem.setEnabled(false);
            SortedSet<String> sortedThemeList = new TreeSet<>();
            sortedThemeList.addAll(imageSets.keySet());
            for (String imageSetIdStr : sortedThemeList) {
                int imageSetId = Integer.parseInt(imageSetIdStr);
                Theme imageSet = imageSets.get(imageSetIdStr);
                CharSequence themeLabel = nounours.getThemeLabel(imageSet);
                themeMenuItem = themesMenu.add(Menu.NONE, imageSetId, imageSetIdx++, themeLabel);
                if (imageSet.getId().equals(curThemeId))
                    themeMenuItem.setEnabled(false);
            }

            final SubMenu updatesMenu = menu.addSubMenu(Menu.NONE, MENU_UPDATES, mainMenuIdx++, R.string.updates);
            updatesMenu.add(Menu.NONE, MENU_LOAD_MORE_THEMES, imageSetIdx++, R.string.loadMoreThemes);

            updatesMenu.add(Menu.NONE, MENU_UPDATE_THEME, imageSetIdx, getString(R.string.upateCurrentTheme, nounours
                    .getCurrentThemeLabel()));
            updatesMenu.setIcon(R.drawable.ic_menu_update);
        }

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
        MenuItem themesMenu = menu.findItem(MENU_THEMES);
        boolean nounoursIsBusy = nounours.isAnimationRunning() || nounours.isLoading();
        boolean hasSDCard = FileUtil.isSdPresent();

        if (themesMenu != null) {
            themesMenu.setEnabled(hasSDCard && !nounoursIsBusy);
            if (hasSDCard && !nounoursIsBusy) {
                String curThemeId = theme == null ? null : theme.getId();
                SubMenu subMenu = themesMenu.getSubMenu();
                MenuItem item = subMenu.findItem(MENU_DEFAULT_THEME);
                if (item != null) {
                    if (Nounours.DEFAULT_THEME_ID.equals(curThemeId))
                        item.setEnabled(false);
                    else
                        item.setEnabled(true);
                }
                for (String themeId : nounours.getThemes().keySet()) {
                    item = subMenu.findItem(Integer.parseInt(themeId));
                    if (!themeId.equals(curThemeId))
                        item.setEnabled(true);
                    else
                        item.setEnabled(false);
                    nounours.debug("enable menu item " + themeId + ": " + item.isEnabled() + ": "
                            + (themeId.equals(curThemeId)));

                }
            }
        }
        MenuItem updatesMenu = menu.findItem(MENU_UPDATES);
        if (updatesMenu != null) {
            updatesMenu.setEnabled(hasSDCard && !nounoursIsBusy);
            SubMenu subMenu = updatesMenu.getSubMenu();
            MenuItem item = subMenu.findItem(MENU_UPDATE_THEME);
            if (theme != null && theme.getId().equals(Nounours.DEFAULT_THEME_ID))
                item.setVisible(false);
            else
                item.setVisible(true);
        }
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
        // The user picked the default image theme
        else if (menuItem.getItemId() == MENU_DEFAULT_THEME) {

            nounours.useTheme(Nounours.DEFAULT_THEME_ID);
            Theme theme = nounours.getDefaultTheme();
            sensorListener.rereadOrientationFile(theme, this);
            return true;
        } else if (menuItem.getItemId() == MENU_LOAD_MORE_THEMES) {
            Runnable themeListUpdater = new Runnable() {
                public void run() {
                    Set<String> curThemeList = nounours.getThemes().keySet();
                    String localThemeFileName = new File(nounours.getAppDir(), "themes.csv").getAbsolutePath();
                    File localThemesFile = new File(localThemeFileName);

                    String remoteThemeFile = nounours.getProperty(Nounours.PROP_THEME_LIST);
                    try {
                        if (!Util.downloadFile(new URI(remoteThemeFile), localThemesFile)) {
                            throw new Exception();

                        }
                        ThemeReader themeReader = new ThemeReader(new FileInputStream(localThemesFile));
                        Map<String, Theme> themes = themeReader.getThemes();
                        if (themes != null) {
                            Set<String> newThemeList = themeReader.getThemes().keySet();
                            if (!newThemeList.equals(curThemeList))
                                nounours.showAlertDialog(getResources().getText(R.string.newThemesAvailable), null);
                            else
                                nounours.showAlertDialog(getResources().getText(R.string.noNewThemes), null);
                        } else
                            throw new Exception("Read 0 themes");
                    } catch (Exception e) {
                        nounours.showAlertDialog(getResources().getText(R.string.errorLoadingThemeList), null);
                        nounours.debug(e);
                    }

                }
            };
            String message = getString(R.string.loadMoreThemesInProgress);
            nounours.runTaskWithProgressBar(themeListUpdater, message, -1);
            return true;

        } else if (menuItem.getItemId() == MENU_UPDATE_THEME) {
            final CharSequence themeLabel = nounours.getCurrentThemeLabel();
            final String message = getString(R.string.updatingCurrentTheme, themeLabel);

            final ThemeUpdateListener updateListener = new ThemeUpdateListener() {

                @Override
                public void updatedFile(String fileName, int fileNumber, int totalFiles, boolean updateOk) {
                    nounours.updateProgressBar(fileNumber, totalFiles, message);

                }
            };
            Runnable updateTheme = new Runnable() {
                public void run() {
                    try {
                        File themeDir = nounours.getAppDir();
                        boolean updated = nounours.getCurrentTheme().update(themeDir.getAbsolutePath(), updateListener);
                        String updateResultMessage;
                        CharSequence themeLabel = nounours.getCurrentThemeLabel();
                        if (updated)
                            updateResultMessage = getString(R.string.updateCurrentThemeComplete, themeLabel);
                        else
                            updateResultMessage = getString(R.string.updateCurrentThemeError);
                        nounours.showAlertDialog(updateResultMessage, null);
                    } catch (Exception e) {
                        nounours.showAlertDialog(getString(R.string.updateCurrentThemeError), null);
                    }
                }
            };
            nounours.runTaskWithProgressBar(updateTheme, message, 100);
            return true;
        }
        // Show an animation or change the theme.
        else {
            final Map<String, Animation> animations = nounours.getAnimations();
            final Map<String, Theme> imageSets = nounours.getThemes();
            final Animation animation = animations.get("" + menuItem.getItemId());
            if (animation != null) {
                nounours.doAnimation(animation);
                return true;
            }
            final Theme imageSet = imageSets.get("" + menuItem.getItemId());
            if (imageSet != null) {
                nounours.useTheme(imageSet.getId());
                sensorListener.rereadOrientationFile(imageSet, this);
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