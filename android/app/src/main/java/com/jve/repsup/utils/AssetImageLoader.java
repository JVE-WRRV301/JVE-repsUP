package com.jve.repsup.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * Loads bitmaps from the app's assets folder using a stored path string.
 *
 * Cosmetic image paths are stored in the database in the form
 * "assets/tops/white_tank.png". The Android {@link android.content.res.AssetManager}
 * is already rooted at the assets folder, so any leading "assets/" segment is
 * stripped before loading.
 *
 * All methods return null (rather than throwing) when a file is missing or
 * cannot be decoded, so callers can fall back to a default layer gracefully.
 *
 * Note: decoding does disk I/O. Call from a background thread, never the UI thread.
 */
public class AssetImageLoader {

    private static final String TAG = "AssetImageLoader";
    private static final String ASSETS_PREFIX = "assets/";

    private AssetImageLoader() {
        // Utility class - no instances
    }

    /**
     * Loads a bitmap from the given assets path.
     *
     * @param context   any Context (application context is fine)
     * @param assetPath path stored in the DB, e.g. "assets/tops/white_tank.png"
     * @return the decoded Bitmap, or null if the path is empty, missing, or undecodable
     */
    public static Bitmap load(Context context, String assetPath) {
        if (context == null || assetPath == null || assetPath.trim().isEmpty()) {
            return null;
        }

        String normalized = normalize(assetPath);

        try (InputStream is = context.getAssets().open(normalized)) {
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (bitmap == null) {
                Log.w(TAG, "Failed to decode asset (not an image?): " + normalized);
            }
            return bitmap;
        } catch (IOException e) {
            // Missing file is expected before real art lands - log at debug level.
            Log.d(TAG, "Asset not found, will fall back: " + normalized);
            return null;
        }
    }

    /**
     * Normalizes a stored path into an AssetManager-relative path.
     * Strips a leading "assets/" and any leading slash.
     */
    static String normalize(String assetPath) {
        String path = assetPath.trim();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.startsWith(ASSETS_PREFIX)) {
            path = path.substring(ASSETS_PREFIX.length());
        }
        return path;
    }
}
