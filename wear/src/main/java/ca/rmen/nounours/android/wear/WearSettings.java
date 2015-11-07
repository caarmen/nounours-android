package ca.rmen.nounours.android.wear;

import ca.rmen.nounours.android.common.settings.NounoursSettings;

public class WearSettings implements NounoursSettings {
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
        return "0";
    }

    @Override
    public int getBackgroundColor() {
        return 0xff000000;
    }
}
