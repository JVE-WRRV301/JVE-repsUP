package com.jve.repsup.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.jve.repsup.R;
import com.jve.repsup.models.Avatar;
import com.jve.repsup.models.CosmeticItem;

import java.util.List;

/**
 * Composites avatar layers (body + outfit) into a single Bitmap.
 * Uses Android Canvas to draw base body, then overlays for top, bottom, shoes, and accessories.
 * Falls back to a default placeholder when specific assets are not yet available.
 */
public class AvatarCompositor {

    private static final String TAG = "AvatarCompositor";
    private static final int AVATAR_SIZE = 512; // px

    /**
     * Composites the full avatar into a single Bitmap.
     *
     * The base body is a bundled drawable resolved from gender + body type + skin
     * tone (e.g. base_female_average_beige). Each equipped cosmetic is drawn from
     * its own asset (resolved from {@link CosmeticItem#getImageData()} via
     * {@link AssetImageLoader}); when an asset cannot be loaded, outfit layers fall
     * back to the bundled default drawable for that type.
     *
     * Paint order (bottom to top):
     *   base body → bottom → socks → top → shoes → hair → accessories
     *
     * Socks sit under shoes so a user can wear socks alone, shoes alone, or both.
     *
     * This method does asset decoding (disk I/O) - call it from a background thread.
     *
     * @param context       Application or Activity context
     * @param gender        "male" or "female"
     * @param bodyType      body type, e.g. "average", "toned", "built"
     * @param skinTone      skin tone, e.g. "grey", "fair", "beige", "tan", "dark"
     * @param equippedItems the resolved CosmeticItems currently equipped (may be null/empty)
     * @return A composited Bitmap of the full avatar
     */
    public static Bitmap composite(Context context, String gender, String bodyType,
                                   String skinTone, List<CosmeticItem> equippedItems) {
        Bitmap result = Bitmap.createBitmap(AVATAR_SIZE, AVATAR_SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        // Layer 1: Base body (bundled drawable keyed off gender + body type + skin tone)
        drawLayer(context, canvas, getBaseBodyResource(context, gender, bodyType, skinTone));

        // Resolve equipped items by type so we can draw them in a fixed layer order.
        CosmeticItem top = findByType(equippedItems, "top");
        CosmeticItem bottom = findByType(equippedItems, "bottom");
        CosmeticItem shoes = findByType(equippedItems, "shoes");
        CosmeticItem socks = findByType(equippedItems, "socks");
        CosmeticItem hair = findByType(equippedItems, "hair");
        CosmeticItem accessories = findByType(equippedItems, "accessory", "accessories");

        // Layer order: bottoms under socks under top; shoes over socks; hair and
        // accessories on top. No bundled default for socks/hair/accessories.
        drawCosmeticLayer(context, canvas, bottom, "bottom");
        drawCosmeticLayer(context, canvas, socks, null);
        drawCosmeticLayer(context, canvas, top, "top");
        drawCosmeticLayer(context, canvas, shoes, "shoes");
        drawCosmeticLayer(context, canvas, hair, null);
        drawCosmeticLayer(context, canvas, accessories, null);

        return result;
    }

    /**
     * Convenience overload that reads gender/body type/skin tone from an Avatar
     * record. Note the Avatar table does not store gender directly, so callers
     * that know the gender should prefer
     * {@link #composite(Context, String, String, String, List)}.
     *
     * @param avatar        the user's Avatar record (body_shape + skin_tone)
     * @param equippedItems the resolved equipped CosmeticItems
     */
    public static Bitmap composite(Context context, Avatar avatar, List<CosmeticItem> equippedItems) {
        String skinTone = avatar != null ? avatar.getSkinTone() : null;
        String bodyType = avatar != null ? avatar.getBodyShape() : null;
        // Gender is not stored on Avatar; default to male when unknown.
        return composite(context, "male", bodyType, skinTone, equippedItems);
    }

    /**
     * Finds the first equipped item matching any of the given types (case-insensitive).
     * Returns null when the list is null/empty or no item matches.
     */
    private static CosmeticItem findByType(List<CosmeticItem> items, String... types) {
        if (items == null || types == null) {
            return null;
        }
        for (CosmeticItem item : items) {
            if (item == null || item.getType() == null) {
                continue;
            }
            for (String type : types) {
                if (type != null && type.equalsIgnoreCase(item.getType())) {
                    return item;
                }
            }
        }
        return null;
    }

    /**
     * Draws a single cosmetic layer from its asset path, falling back to the
     * bundled default drawable for the given type when the asset is unavailable.
     *
     * @param defaultType type key for the fallback drawable ("top"/"bottom"/"shoes"),
     *                    or null to skip the fallback (e.g. accessories have no default)
     */
    private static void drawCosmeticLayer(Context context, Canvas canvas,
                                          CosmeticItem item, String defaultType) {
        if (item == null) {
            return;
        }

        Bitmap layer = AssetImageLoader.load(context, item.getImageData());
        if (layer != null) {
            Rect dst = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
            canvas.drawBitmap(layer, null, dst, null);
            layer.recycle();
            return;
        }

        // Asset missing - fall back to the bundled default for this type, if any.
        if (defaultType != null) {
            Log.d(TAG, "No asset for equipped " + defaultType + " (id=" + item.getCosmeticId()
                    + "), using default layer");
            drawLayer(context, canvas, getDefaultOutfitResource(defaultType));
        } else {
            Log.d(TAG, "No asset for equipped accessory (id=" + item.getCosmeticId()
                    + ") and no default - skipping layer");
        }
    }

    /**
     * Returns the full-body initial avatar resource for the "Meet Your Avatar" screen.
     * This is a single pre-rendered image (greyscale, default outfit, waving pose)
     * based on gender and body type only.
     *
     * Body types: skinny, average, toned, built, chubby, overweight
     *
     * @param gender   "male" or "female"
     * @param bodyType "skinny", "average", "toned", "built", "chubby", or "overweight"
     * @return Drawable resource ID for the full initial avatar
     */
    public static int getInitialAvatarResource(String gender, String bodyType) {
        if (gender == null || bodyType == null) {
            return R.drawable.avatar_male_average;
        }

        String g = gender.toLowerCase().trim();
        String bt = bodyType.toLowerCase().trim();

        String key = g + "_" + bt;
        switch (key) {
            case "male_skinny":
                return R.drawable.avatar_male_skinny;
            case "male_average":
                return R.drawable.avatar_male_average;
            case "male_toned":
                return R.drawable.avatar_male_toned;
            case "male_built":
                return R.drawable.avatar_male_built;
            case "male_chubby":
                return R.drawable.avatar_male_chubby;
            case "male_overweight":
                return R.drawable.avatar_male_overweight;
            case "female_skinny":
                return R.drawable.avatar_female_skinny;
            case "female_average":
                return R.drawable.avatar_female_average;
            case "female_toned":
                return R.drawable.avatar_female_toned;
            case "female_built":
                return R.drawable.avatar_female_built;
            case "female_chubby":
                return R.drawable.avatar_female_chubby;
            case "female_overweight":
                return R.drawable.avatar_female_overweight;
            default:
                // Default to male average if unknown combination
                return R.drawable.avatar_male_average;
        }
    }

    /** Skin tone used before the user picks one in the locker. */
    public static final String DEFAULT_SKIN_TONE = "grey";

    /**
     * Resolves the base body drawable for a gender + body type + skin tone combo,
     * following the naming convention base_{gender}_{bodytype}_{skintone}
     * (e.g. base_female_average_beige, base_male_toned_grey).
     *
     * Resolution is by resource name via {@link android.content.res.Resources#getIdentifier},
     * so new body/tone drawables are picked up automatically once added to res/drawable.
     * Falls back to a grey variant of the combo, then to the universal default, so a
     * missing tone never crashes rendering.
     *
     * @param gender   "male" or "female"
     * @param bodyType body type, e.g. "average", "toned", "built", "chubby", "overweight", "skinny"
     * @param skinTone skin tone, e.g. "grey", "fair", "beige", "tan", "dark"
     * @return a drawable resource ID for the base body
     */
    public static int getBaseBodyResource(Context context, String gender, String bodyType, String skinTone) {
        String g = normalize(gender, "male");
        String bt = normalize(bodyType, "average");
        String tone = normalize(skinTone, DEFAULT_SKIN_TONE);

        // Try the exact combination first, then a grey fallback of the same body,
        // then the universal default placeholder.
        int res = resolveDrawable(context, "base_" + g + "_" + bt + "_" + tone);
        if (res != 0) {
            return res;
        }
        res = resolveDrawable(context, "base_" + g + "_" + bt + "_" + DEFAULT_SKIN_TONE);
        if (res != 0) {
            Log.d(TAG, "No base for tone '" + tone + "', falling back to grey for "
                    + g + "_" + bt);
            return res;
        }
        Log.d(TAG, "No base drawable for " + g + "_" + bt + "_" + tone + ", using default");
        return R.drawable.ic_avatar_body_default;
    }

    /**
     * Looks up a drawable resource ID by name. Returns 0 when not found.
     */
    private static int resolveDrawable(Context context, String name) {
        if (context == null || name == null) {
            return 0;
        }
        return context.getResources().getIdentifier(
                name, "drawable", context.getPackageName());
    }

    /**
     * Lowercases and trims a value, returning the fallback when null/blank.
     */
    private static String normalize(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String v = value.toLowerCase().trim();
        return v.isEmpty() ? fallback : v;
    }

    /**
     * Returns the drawable resource for a default outfit piece.
     *
     * @param type "top", "bottom", or "shoes"
     * @return Drawable resource ID for the outfit piece
     */
    public static int getDefaultOutfitResource(String type) {
        if (type == null) {
            return R.drawable.ic_avatar_body_default;
        }

        switch (type.toLowerCase().trim()) {
            case "top":
                return R.drawable.ic_avatar_top_default;
            case "bottom":
                return R.drawable.ic_avatar_bottom_default;
            case "shoes":
                return R.drawable.ic_avatar_shoes_default;
            default:
                return R.drawable.ic_avatar_body_default;
        }
    }

    /**
     * Draws a single layer onto the canvas from a drawable resource.
     * Gracefully handles missing resources by logging a warning and skipping the layer.
     */
    private static void drawLayer(Context context, Canvas canvas, int resourceId) {
        try {
            Drawable drawable = ContextCompat.getDrawable(context, resourceId);
            if (drawable != null) {
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
            } else {
                Log.w(TAG, "Drawable resource not found: " + resourceId);
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to draw layer with resource: " + resourceId, e);
        }
    }
}
