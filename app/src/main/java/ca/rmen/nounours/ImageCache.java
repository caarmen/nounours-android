package ca.rmen.nounours;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ca.rmen.nounours.data.Image;
import ca.rmen.nounours.util.BitmapUtil;
import ca.rmen.nounours.util.FileUtil;
import ca.rmen.nounours.util.Trace;

/**
 * Created by calvarez on 26/09/15.
 */
public class ImageCache {

    interface ImageCacheListener {
        void onImageLoaded(Image image, int progress, int total);
    }

    private static final Map<String, Bitmap> imageCache = new ConcurrentHashMap<>();
    private final Context context;
    private final ImageCacheListener listener;

    ImageCache(Context context, ImageCacheListener listener) {
        this.context = context;
        this.listener = listener;
    }

    /**
     * Load the images into memory.
     */
    boolean cacheImages(Collection<Image> images) {
        int i = 0;
        int max = images.size();
        for (final Image image : images) {
            Bitmap bitmap = loadImage(image);
            if (bitmap == null)
                return false;
            listener.onImageLoaded(image, i++, max);
        }
        return true;
    }

    void clearImageCache() {

        for (Bitmap bitmap : imageCache.values()) {
            if (!bitmap.isRecycled()) bitmap.recycle();
        }
        imageCache.clear();
        System.gc();

    }

    /**
     * Find the Android image for the given nounours image.
     */
    Bitmap getDrawableImage(final Image image) {
        Bitmap res = imageCache.get(image.getId());
        if (res == null) {
            Trace.debug(this, "Loading drawable image " + image);
            res = loadImage(image);
        }
        return res;
    }

    /**
     * Load an image from the disk into memory. Return the Drawable for the
     * image.
     */
    private Bitmap loadImage(final Image image) {
        Trace.debug(this, "Loading " + image + " into memory");
        // This is one of the downloaded images, in the sdcard.
        if (image.getFilename().contains(FileUtil.getSdFolder(context).getAbsolutePath())) {
            // Load the new image
            Trace.debug(this, "Load themed image.");
            Bitmap newBitmap = BitmapUtil.loadBitmap(context, image.getFilename());
            return copyAndCacheImage(newBitmap, image.getId());
        }
        // This is one of the default images bundled in the apk.
        else {
            final int imageResId = context.getResources().getIdentifier(image.getFilename(), "drawable",
                    context.getClass().getPackage().getName());
            // Load the image from the resource file.
            Trace.debug(this, "Load default image " + imageResId);
            Bitmap readOnlyBitmap = BitmapUtil.loadBitmap(context, imageResId);
            Trace.debug(this, "default image mutable = " + readOnlyBitmap.isMutable() + ", recycled="
                    + readOnlyBitmap.isRecycled());
            // Store the newly loaded drawable in cache for the first time.
            // Make a mutable copy of the drawable.
            return copyAndCacheImage(readOnlyBitmap, image.getId());
        }
    }

    /**
     * Create a mutable copy of the given immutable bitmap, and store it in the
     * cache.
     *
     * @param readOnlyBitmap the immutable bitmap
     * @return the mutable copy of the read-only bitmap.
     */
    private Bitmap copyAndCacheImage(Bitmap readOnlyBitmap, String imageId) {
        Bitmap mutableBitmap = readOnlyBitmap.copy(readOnlyBitmap.getConfig(), true);
        Canvas canvas = new Canvas(mutableBitmap);
        canvas.drawBitmap(readOnlyBitmap, 0, 0, null);
        readOnlyBitmap.recycle();
        synchronized (imageCache) {
            imageCache.put(imageId, mutableBitmap);
        }
        return mutableBitmap;
    }

}
