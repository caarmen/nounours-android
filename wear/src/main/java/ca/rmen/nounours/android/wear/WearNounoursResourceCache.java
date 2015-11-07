package ca.rmen.nounours.android.wear;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;

import ca.rmen.nounours.android.common.nounours.cache.ImageCache;
import ca.rmen.nounours.android.common.nounours.cache.NounoursResourceCache;
import ca.rmen.nounours.data.Image;
import ca.rmen.nounours.data.Theme;

public class WearNounoursResourceCache implements NounoursResourceCache {

    private final Context mContext;
    private final ImageCache mImageCache;
    private final Handler mUiHandler;

    public WearNounoursResourceCache(Context context) {
        mContext = context;
        mImageCache = new ImageCache();
        mUiHandler = new Handler();
    }
    @Override
    public boolean loadImages(Theme theme, ImageCache.ImageCacheListener imageCacheListener) {
        return mImageCache.cacheImages(mContext, theme.getImages().values(), mUiHandler, imageCacheListener);
    }

    @Override
    public Bitmap getDrawableImage(Context context, Image image) {
        return mImageCache.getDrawableImage(context, image);
    }

    @Override
    public void freeImages() {
        mImageCache.clearImageCache();
    }

    @Override
    public boolean loadSounds(Theme theme) {
        return true;
    }

    @Override
    public void freeSounds() {

    }
}
