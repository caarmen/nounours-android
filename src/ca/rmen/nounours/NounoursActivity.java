/*
 * Copyright (c) 2009 Carmen Alvarez. All Rights Reserved.
 *
 */
package ca.rmen.nounours;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.Map;
import java.util.Set;

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
import ca.rmen.nounours.data.Theme;
import ca.rmen.nounours.io.ThemeReader;
import ca.rmen.nounours.util.FileUtil;

import com.nullwire.trace.ExceptionHandler;

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
    private static final int MENU_UPDATES = 1007;
    private static final int MENU_LOAD_MORE_THEMES = 1008;
    private static final int MENU_UPDATE_THEME = 1009;

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
        setupAnimationMenu(actionMenu);

        // Set up the toggle sound menu
        final MenuItem toggleSoundMenu = menu.add(Menu.NONE, MENU_TOGGLE_SOUND, mainMenuIdx++, R.string.disablesound);
        toggleSoundMenu.setIcon(R.drawable.ic_volume_off_small);

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
            for (Theme imageSet : imageSets.values()) {
                int imageSetId = Integer.parseInt(imageSet.getId());
                CharSequence themeLabel = nounours.getThemeLabel(imageSet);
                themeMenuItem = themesMenu.add(Menu.NONE, imageSetId, imageSetIdx++, themeLabel);
                if (imageSet.getId().equals(curThemeId))
                    themeMenuItem.setEnabled(false);
            }

            final SubMenu updatesMenu = menu.addSubMenu(Menu.NONE, MENU_UPDATES, mainMenuIdx++, R.string.updates);
            updatesMenu.add(Menu.NONE, MENU_LOAD_MORE_THEMES, imageSetIdx++, R.string.loadMoreThemes);

            updatesMenu.add(Menu.NONE, MENU_UPDATE_THEME, imageSetIdx++, getString(R.string.upateCurrentTheme, nounours
                    .getCurrentThemeLabel()));
        }

        // Set up the help menu
        final MenuItem helpMenu = menu.add(Menu.NONE, MENU_HELP, mainMenuIdx++, R.string.help);
        helpMenu.setIcon(R.drawable.ic_menu_help);

        // Set up the about menu
        final MenuItem aboutMenu = menu.add(Menu.NONE, MENU_ABOUT, mainMenuIdx++, R.string.about);
        aboutMenu.setIcon(R.drawable.ic_menu_info_details);
        return true;
    }

    private void setupAnimationMenu(SubMenu actionMenu) {
        int actionMenuIdx = 0;
        actionMenu.clear();
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
        Theme theme = nounours.getCurrentTheme();
        MenuItem themesMenu = menu.findItem(MENU_THEMES);
        if (themesMenu != null) {
            boolean enableThemes = true;
            if (nounours.isAnimationRunning() || !FileUtil.isSdPresent())
                enableThemes = false;
            else {
                themesMenu.setEnabled(enableThemes);
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
            SubMenu subMenu = updatesMenu.getSubMenu();
            MenuItem item = subMenu.findItem(MENU_UPDATE_THEME);
            CharSequence curThemeLabel = nounours.getCurrentThemeLabel();
            getResources().getText(R.string.upateCurrentTheme, curThemeLabel);
            if (theme != null && theme.getId().equals(Nounours.DEFAULT_THEME_ID))
                item.setEnabled(false);
            else
                item.setEnabled(true);
        }
        MenuItem animationMenu = menu.findItem(MENU_ACTION);
        if (animationMenu != null) {
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
            nounours.setEnableVibrate(!nounours.isVibrateEnabled());
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

            nounours.useTheme(Nounours.DEFAULT_THEME_ID);
            Theme theme = nounours.getDefaultTheme();
            sensorListener.rereadOrientationFile(theme, this);
            return true;
        } else if (menuItem.getItemId() == MENU_LOAD_MORE_THEMES) {
            Runnable themeListUpdater = new Runnable() {
                public void run() {
                    Set<String> curThemeList = nounours.getThemes().keySet();
                    String localThemeFileName = nounours.getProperty(Nounours.PROP_DOWNLOADED_IMAGES_DIR)
                            + File.separator + "themes.csv";
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
                            if (!newThemeList.equals(curThemeList)) {
                                nounours.showAlertDialog(getResources().getText(R.string.newThemesAvailable), null);
                            }
                            nounours.showAlertDialog(getResources().getText(R.string.noNewThemes), null);
                        }
                        throw new Exception();
                    } catch (Exception e) {
                        nounours.showAlertDialog(getResources().getText(R.string.errorLoadingThemeList), null);
                    }

                }
            };
            String message = getString(R.string.loadMoreThemesInProgress);
            nounours.runTaskWithProgressBar(themeListUpdater, false, message, -1);
            return true;

        } else if (menuItem.getItemId() == MENU_UPDATE_THEME) {

            Runnable updateTheme = new Runnable() {
                public void run() {
                    try {
                        boolean updated = nounours.getCurrentTheme().update(
                                nounours.getProperty(Nounours.PROP_DOWNLOADED_IMAGES_DIR));
                        String message = null;
                        CharSequence themeLabel = nounours.getCurrentThemeLabel();
                        if (updated)
                            message = getString(R.string.updateCurrentThemeComplete, themeLabel);
                        else
                            message = getString(R.string.themeLoadError);
                        nounours.showAlertDialog(message, null);
                    } catch (Exception e) {
                        nounours.showAlertDialog(getString(R.string.themeLoadError), null);
                    }
                }
            };
            CharSequence themeLabel = nounours.getCurrentThemeLabel();
            String message = getString(R.string.updatingCurrentTheme, themeLabel);
            nounours.runTaskWithProgressBar(updateTheme, false, message, -1);
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