package ca.rmen.nounours.android.wear;

import ca.rmen.nounours.android.common.settings.NounoursSettings;

public class WearSettings implements NounoursSettings {
    private final String mThemeId;
    private final int mBackgroundColor;

    public WearSettings(String themeId, int backgroundColor) {
        mThemeId = themeId;
        mBackgroundColor = backgroundColor;
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

    @Override
    public int getBackgroundColor() {
        return mBackgroundColor;
    }
}
