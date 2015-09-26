package ca.rmen.nounours;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ca.rmen.nounours.data.Image;
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
            Bitmap bitmap = loadImage(image, 10);
            if (bitmap == null)
                return false;
            listener.onImageLoaded(image, i++, max);
        }
        return true;
    }

    void clearImageCache() {

        for (Bitmap bitmap : imageCache.values()) {
            if (!bitmap.isRecycled())
                bitmap.recycle();
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
            res = loadImage(image, 10);
        }
        return res;
    }

    /**
     * Load an image from the disk into memory. Return the Drawable for the
     * image.
     *
     * @param retries number of attempts to scale down the image if we run out of memory.
     */
    private Bitmap loadImage(final Image image, int retries) {
        Trace.debug(this, "Loading " + image + " into memory");
        Bitmap cachedBitmap = imageCache.get(image.getId());
        Bitmap newBitmap;
        try {
            // This is one of the downloaded images, in the sdcard.
            if (image.getFilename().contains(FileUtil.getSdFolder(context).getAbsolutePath())) {
                // Load the new image
                Trace.debug(this, "Load themed image.");
                newBitmap = BitmapFactory.decodeFile(image.getFilename());
                // If the image is corrupt or missing, use the default image.
                if (newBitmap == null) {
                    return null;
                }
            }
            // This is one of the default images bundled in the apk.
            else {
                final int imageResId = context.getResources().getIdentifier(image.getFilename(), "drawable",
                        context.getClass().getPackage().getName());
                // Load the image from the resource file.
                Trace.debug(this, "Load default image " + imageResId);
                Bitmap readOnlyBitmap = BitmapFactory.decodeResource(context.getResources(), imageResId);// ((BitmapDrawable)
                // context.getResources().getDrawable(imageResId)).getBitmap();
                Trace.debug(this, "default image mutable = " + readOnlyBitmap.isMutable() + ", recycled="
                        + readOnlyBitmap.isRecycled());
                // Store the newly loaded drawable in cache for the first time.
                if (cachedBitmap == null) {
                    // Make a mutable copy of the drawable.
                    cachedBitmap = copyAndCacheImage(readOnlyBitmap, image.getId());
                    return cachedBitmap;
                }
                newBitmap = readOnlyBitmap;
            }
            if (cachedBitmap == null) {
                Trace.debug(this, "Image not in cache");
            } else if (cachedBitmap.isRecycled()) {
                Trace.debug(this, "Cached image was recycled!");
            } else
                cachedBitmap.recycle();

            // No cached bitmap, using a theme. This will happen if the user
            // loads
            // the app up with a non-default theme.
            cachedBitmap = copyAndCacheImage(newBitmap, image.getId());
            return cachedBitmap;
        } catch (OutOfMemoryError error) {
            Trace.debug(this, "Memory error loading " + image + ". " + retries + " retries left");
            if (retries > 0) {
                System.gc();
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return loadImage(image, retries - 1);
            }
            return null;
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
