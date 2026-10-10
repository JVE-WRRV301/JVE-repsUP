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

    /** Art key of the default white socks every avatar wears when none are equipped. */
    private static final String DEFAULT_SOCKS_KEY = "white_socks_default";

    /**
     * How different a pixel must be from a placeholder's background colour (sum of
     * the red, green and blue differences) to count as part of the item.
     */
    private static final int PLACEHOLDER_BACKGROUND_TOLERANCE = 100;

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
     *   base body → socks → bottom → shoes → top → hair → accessories
     *
     * Socks sit under shoes so a user can wear socks alone, shoes alone, or both.
     * Shoes are drawn over the bottoms, so tights and trousers sit behind high-top
     * shoes instead of covering them. Every avatar always wears socks: when none
     * are equipped the gender's default white socks are drawn.
     *
     * This method does asset decoding (disk I/O) - call it from a background thread.
     *
     * @param context       Application or Activity context
     * @param gender        "male" or "female"
     * @param bodyType      body type: male = average/built/chubby, female = average/toned/chubby
     * @param skinTone      skin tone, e.g. "grey", "fair", "beige", "tan", "dark"
     * @param equippedItems the resolved CosmeticItems currently equipped (may be null/empty)
     * @return A composited Bitmap of the full avatar
     */
    public static Bitmap composite(Context context, String gender, String bodyType,
                                   String skinTone, String hairColour,
                                   List<CosmeticItem> equippedItems) {
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

        // Avatars always wear socks. Older accounts, or an outfit saved without a
        // socks row, would otherwise show bare feet.
        if (socks == null) {
            socks = new CosmeticItem();
            socks.setType("socks");
            socks.setImageData(DEFAULT_SOCKS_KEY);
        }

        // Layer order (bottom to top of the stack):
        //   socks -> bottom -> shoes -> top -> hair -> accessories
        // Socks sit lowest. Shoes are drawn over the bottoms so tights and long
        // trousers fall behind high-top shoes rather than over them.
        drawClothingLayer(context, canvas, socks, gender, bodyType);
        drawClothingLayer(context, canvas, bottom, gender, bodyType);
        drawClothingLayer(context, canvas, shoes, gender, bodyType);
        drawClothingLayer(context, canvas, top, gender, bodyType);
        drawHairLayer(context, canvas, hair, gender, hairColour);
        drawClothingLayer(context, canvas, accessories, gender, bodyType);

        return result;
    }

    /**
     * Fraction of the avatar's height (from the top) that contains the head,
     * hair, and neck. Used to crop a face thumbnail from the full-body render.
     * Assumes the figure is drawn roughly head-at-top on the 512x512 canvas.
     */
    private static final float FACE_CROP_FRACTION = 0.22f;

    /**
     * Composites the avatar and returns just the head/hair/neck region as a
     * square bitmap, for use as a profile picture (e.g. the top-bar thumbnail).
     *
     * It renders the full avatar, crops the top {@link #FACE_CROP_FRACTION} of the
     * height centered horizontally into a square, so only the face and hair show.
     *
     * Does asset decoding (disk I/O) - call from a background thread.
     */
    public static Bitmap compositeFace(Context context, String gender, String bodyType,
                                       String skinTone, String hairColour,
                                       List<CosmeticItem> equippedItems) {
        Bitmap full = composite(context, gender, bodyType, skinTone, hairColour, equippedItems);

        int w = full.getWidth();
        int h = full.getHeight();

        // Only look at the top portion of the figure (head + hair + neck). This
        // avoids the torso/arms pulling the crop's centre downward or sideways.
        int scanHeight = Math.max(1, Math.round(h * FACE_CROP_FRACTION));

        int[] pixels = new int[w * scanHeight];
        full.getPixels(pixels, 0, w, 0, 0, w, scanHeight);

        // Find the bounding box of the opaque (drawn) pixels in that top band, so
        // we can centre the crop on the head itself rather than the raw canvas.
        int minX = w, minY = scanHeight, maxX = -1, maxY = -1;
        final int alphaThreshold = 12; // ignore near-transparent anti-aliasing fuzz
        for (int y = 0; y < scanHeight; y++) {
            int rowOffset = y * w;
            for (int x = 0; x < w; x++) {
                int alpha = (pixels[rowOffset + x] >>> 24);
                if (alpha > alphaThreshold) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }

        int left;
        int top;
        int size;

        if (maxX < minX || maxY < minY) {
            // Nothing opaque found (shouldn't happen) - fall back to the old
            // top-centred square crop.
            size = Math.max(1, Math.min(w, scanHeight));
            left = (w - size) / 2;
            top = 0;
        } else {
            int contentW = maxX - minX + 1;
            int contentH = maxY - minY + 1;

            // Square crop sized to the larger content dimension, padded slightly so
            // the head/hair doesn't touch the circle edge, then centred on the head.
            // Kept tight so the picture zooms into the hair/face/neck (down to the
            // t-shirt neckline) rather than showing the shoulders.
            size = Math.max(contentW, contentH);
            int padding = Math.round(size * 0.06f);
            size += padding * 2;

            int centerX = minX + contentW / 2;
            int centerY = minY + contentH / 2;
            left = centerX - size / 2;
            top = centerY - size / 2;

            // Clamp the square to the full render's bounds.
            if (size > w) size = w;
            if (size > h) size = h;
            if (left < 0) left = 0;
            if (top < 0) top = 0;
            if (left + size > w) left = w - size;
            if (top + size > h) top = h - size;
        }

        Bitmap face = Bitmap.createBitmap(full, left, top, size, size);

        // createBitmap may return the same instance; only recycle if it's a copy.
        if (face != full) {
            full.recycle();
        }
        return face;
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
     * Default body type folder used when a garment has no art for the user's
     * specific body type (only average + built exist so far).
     */
    private static final String DEFAULT_BODY_TYPE = "average";

    /**
     * Draws a clothing layer (top/bottom/shoes/socks/accessory), resolving the
     * per-body-type asset:
     *   assets/{type}/{gender}-{bodytype}/{itemKey}.png
     * Falls back to the gender's "average" folder when the specific body type
     * has no art for this garment. The item's imageData holds the item KEY
     * (e.g. "white_tshirt"), not a full path.
     */
    private static void drawClothingLayer(Context context, Canvas canvas,
                                          CosmeticItem item, String gender, String bodyType) {
        if (item == null || item.getImageData() == null) {
            return;
        }
        Bitmap layer = loadClothingBitmap(context, item, gender, bodyType);
        if (layer != null) {
            canvas.drawBitmap(layer, null, new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), null);
            layer.recycle();
        } else {
            Log.d(TAG, "No asset for " + item.getType() + " '" + item.getImageData() + "'");
        }
    }

    /**
     * Loads a clothing item's bitmap for the given gender + body type, trying the
     * exact body type folder then falling back to the gender's "average" folder.
     * Shared by the compositor and the locker thumbnails so they always agree.
     * Returns null when no asset is found.
     */
    public static Bitmap loadClothingBitmap(Context context, CosmeticItem item,
                                            String gender, String bodyType) {
        if (item == null || item.getImageData() == null) {
            return null;
        }
        String folderType = folderForType(item.getType() != null ? item.getType().toLowerCase() : "");
        String g = normalize(gender, "male");
        String bt = normalize(bodyType, DEFAULT_BODY_TYPE);
        String key = item.getImageData().trim();

        Bitmap layer = AssetImageLoader.load(context,
                "assets/" + folderType + "/" + g + "-" + bt + "/" + key + ".png");
        if (layer == null && !bt.equals(DEFAULT_BODY_TYPE)) {
            layer = AssetImageLoader.load(context,
                    "assets/" + folderType + "/" + g + "-" + DEFAULT_BODY_TYPE + "/" + key + ".png");
        }
        return layer;
    }

    /**
     * Draws the hair layer, resolving by hairstyle key + colour:
     *   assets/hair/{gender}-hair/{colour}_{styleKey}.png
     * Falls back to the grey (default) colour when the chosen colour has no art.
     * The item's imageData holds the hairstyle KEY (e.g. "default_hairstyle").
     */
    private static void drawHairLayer(Context context, Canvas canvas,
                                      CosmeticItem hair, String gender, String hairColour) {
        if (hair == null || hair.getImageData() == null) {
            return;
        }
        Bitmap layer = loadHairBitmap(context, hair, gender, hairColour);
        if (layer != null) {
            canvas.drawBitmap(layer, null, new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), null);
            layer.recycle();
        } else {
            Log.d(TAG, "No hair asset for '" + hair.getImageData() + "' colour '" + hairColour + "'");
        }
    }

    /**
     * Loads a hairstyle's bitmap for the given gender + colour, falling back to
     * the grey (default) colour when the chosen colour has no art. Shared by the
     * compositor and the locker thumbnails. Returns null when no asset is found.
     */
    public static Bitmap loadHairBitmap(Context context, CosmeticItem hair,
                                        String gender, String hairColour) {
        if (hair == null || hair.getImageData() == null) {
            return null;
        }
        String g = normalize(gender, "male");
        String key = hair.getImageData().trim();

        // Set-colour (non-colourable) hairstyles bake the colour into the file, so
        // imageData already holds the full filename stem (e.g. "black_cornrows_hairstyle").
        // Load it as-is without prepending the chosen colour.
        if (!hair.isColourable()) {
            return AssetImageLoader.load(context,
                    "assets/hair/" + g + "-hair/" + key + ".png");
        }

        // Colourable hairstyles (e.g. the default) store only the style key; the
        // compositor builds {colour}_{styleKey}.png. Try the chosen colour first,
        // then grey, then any other known colour so the hair still renders even
        // when a particular colour has no art for this gender (e.g. female hair
        // has no grey_default_hairstyle, so a new grey-default female still shows
        // hair by falling through to brown/black/etc.).
        String colour = normalize(hairColour, DEFAULT_HAIR_COLOUR);
        Bitmap layer = AssetImageLoader.load(context,
                "assets/hair/" + g + "-hair/" + colour + "_" + key + ".png");
        if (layer != null) {
            return layer;
        }
        for (String fallback : HAIR_COLOUR_FALLBACKS) {
            if (fallback.equals(colour)) {
                continue;
            }
            layer = AssetImageLoader.load(context,
                    "assets/hair/" + g + "-hair/" + fallback + "_" + key + ".png");
            if (layer != null) {
                return layer;
            }
        }
        return null;
    }

    /**
     * Colour fallbacks for colourable hair when the chosen colour has no art.
     * Ordered so grey (the pre-selection default) is tried first, then common
     * colours. Ensures hair renders even where a gender lacks a grey variant.
     */
    private static final String[] HAIR_COLOUR_FALLBACKS = {"grey", "brown", "black", "blonde", "ginger"};

    /** Default hair colour before the user picks one (matches grey base art). */
    public static final String DEFAULT_HAIR_COLOUR = "grey";

    /**
     * Maps a cosmetic type (singular, as stored in the DB) to its asset subfolder
     * (plural on disk): top->tops, bottom->bottoms, shoe(s)->shoes, sock(s)->socks,
     * accessory->accessories.
     */
    private static String folderForType(String type) {
        if (type == null) {
            return "";
        }
        switch (type) {
            case "top":         return "tops";
            case "bottom":      return "bottoms";
            case "shoe":
            case "shoes":       return "shoes";
            case "sock":
            case "socks":       return "socks";
            case "accessory":
            case "accessories": return "accessories";
            default:            return type;
        }
    }

    /**
     * Returns the full-body initial avatar resource for the "Meet Your Avatar" screen.
     * This is a single pre-rendered image (greyscale, default outfit, waving pose)
     * based on gender and body type only.
     *
     * Supported body types: male = average/built/chubby, female = average/toned/chubby.
     *
     * @param gender   "male" or "female"
     * @param bodyType male: "average"/"built"/"chubby"; female: "average"/"toned"/"chubby"
     * @return Drawable resource ID for the full initial avatar
     */
    public static int getInitialAvatarResource(String gender, String bodyType) {
        if (gender == null || bodyType == null) {
            return R.drawable.avatar_male_average;
        }

        String g = gender.toLowerCase().trim();
        String bt = bodyType.toLowerCase().trim();

        // Only three body types per gender are supported:
        //   Male:   average, built, chubby
        //   Female: average, toned, chubby
        String key = g + "_" + bt;
        switch (key) {
            case "male_average":
                return R.drawable.avatar_male_average;
            case "male_built":
                return R.drawable.avatar_male_built;
            case "male_chubby":
                return R.drawable.avatar_male_chubby;
            case "female_average":
                return R.drawable.avatar_female_average;
            case "female_toned":
                return R.drawable.avatar_female_toned;
            case "female_chubby":
                return R.drawable.avatar_female_chubby;
            default:
                // Default to average of the requested gender for any unknown combo.
                return "female".equals(g)
                        ? R.drawable.avatar_female_average
                        : R.drawable.avatar_male_average;
        }
    }

    /** Skin tone used before the user picks one in the locker. */
    public static final String DEFAULT_SKIN_TONE = "grey";

    /**
     * Resolves the base body drawable for a gender + body type + skin tone combo,
     * following the naming convention base_{gender}_{bodytype}_{skintone}
     * (e.g. base_female_average_tan, base_male_built_grey).
     *
     * Resolution is by resource name via {@link android.content.res.Resources#getIdentifier},
     * so new body/tone drawables are picked up automatically once added to res/drawable.
     * Falls back to a grey variant of the combo, then to the universal default, so a
     * missing tone never crashes rendering.
     *
     * @param gender   "male" or "female"
     * @param bodyType body type: male = "average"/"built"/"chubby", female = "average"/"toned"/"chubby"
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
     * Finds the box around the visible (non-transparent) pixels of a bitmap.
     * Returns the whole bitmap's bounds when nothing is visible.
     */
    public static android.graphics.Rect opaqueBounds(Bitmap bmp) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int[] row = new int[w];
        int left = w, right = -1, top = h, bottom = -1;
        for (int y = 0; y < h; y++) {
            bmp.getPixels(row, 0, w, 0, y, w, 1);
            for (int x = 0; x < w; x++) {
                if ((row[x] >>> 24) > 16) {
                    if (x < left) left = x;
                    if (x > right) right = x;
                    if (y < top) top = y;
                    bottom = y;
                }
            }
        }
        if (right < 0) {
            return new android.graphics.Rect(0, 0, w, h);
        }
        return new android.graphics.Rect(left, top, right + 1, bottom + 1);
    }

    /**
     * Loads a locker THUMBNAIL for an item, cropped tightly to the garment so it
     * fills the cell instead of appearing tiny in its natural body position.
     *
     * Each cosmetic PNG is drawn on a full-body canvas (e.g. shoes sit at the very
     * bottom, hair at the very top, tops in the middle). If we scaled that whole
     * canvas into a small square cell the actual item would be tiny and hard to
     * see. Here we decode the item's art, find the bounding box of its opaque
     * (non-transparent) pixels, and return just that region as a square bitmap.
     * The caller can then scale that square to fill the cell, so every item —
     * regardless of where it sits on the body — is zoomed in and centred at a
     * comparable size.
     *
     * Does asset decoding (disk I/O) - call from a background thread.
     *
     * @param context    any Context (application context is fine)
     * @param item       the cosmetic item to thumbnail
     * @param gender     "male" or "female"
     * @param bodyType   body type (for clothing) - ignored for hair
     * @param hairColour chosen hair colour (for hair) - ignored for clothing
     * @return a tightly-cropped square bitmap of the item, or null when no art exists
     */
    public static Bitmap loadItemThumbnail(Context context, CosmeticItem item,
                                           String gender, String bodyType, String hairColour) {
        if (item == null || item.getType() == null) {
            return null;
        }

        // Placeholder items are non-wearable filler: a single opaque .jpeg per type
        // (not body-type specific) shown locked in the locker. They have no
        // transparency, so crop to whatever differs from the grey background.
        if (isPlaceholder(item)) {
            return loadCroppedPlaceholder(context, item, gender);
        }

        boolean isHair = "hair".equalsIgnoreCase(item.getType());
        Bitmap raw = isHair
                ? loadHairBitmap(context, item, gender, hairColour)
                : loadClothingBitmap(context, item, gender, bodyType);
        if (raw == null) {
            // Some wearable items intentionally have no body overlay (e.g. the
            // female default bottom: the base body already shows black shorts, so
            // it draws nothing when equipped). For the locker we still want a
            // thumbnail, so fall back to a same-key .jpeg in the placeholder folder.
            return loadCroppedPlaceholder(context, item, gender);
        }
        Bitmap cropped = cropToOpaqueSquare(raw);
        if (cropped != raw) {
            raw.recycle();
        }
        return cropped;
    }

    /**
     * Unlock type marking a non-wearable placeholder cosmetic. These sit locked in
     * the locker as "coming soon" filler and have no compositable art.
     */
    public static final String PLACEHOLDER_UNLOCK_TYPE = "placeholder";

    /** True when the item is a non-wearable placeholder filler item. */
    public static boolean isPlaceholder(CosmeticItem item) {
        return item != null && PLACEHOLDER_UNLOCK_TYPE.equalsIgnoreCase(item.getUnlockType());
    }

    /**
     * Loads a placeholder item's thumbnail from its single per-type folder:
     *   assets/{type}/{gender}-placeholders/{key}.jpeg
     * Placeholders aren't body-type specific and use the .jpeg extension.
     * Returns null when the asset is missing.
     */
    public static Bitmap loadPlaceholderBitmap(Context context, CosmeticItem item, String gender) {
        if (item == null || item.getImageData() == null) {
            return null;
        }
        String folderType = folderForType(item.getType() != null ? item.getType().toLowerCase() : "");
        String g = normalize(gender, "male");
        String key = item.getImageData().trim();
        return AssetImageLoader.load(context,
                "assets/" + folderType + "/" + g + "-placeholders/" + key + ".jpeg");
    }

    /**
     * Loads a placeholder picture and crops it to the item, so small items such as
     * shoes (which sit tiny at the bottom of a big grey picture) are zoomed in like
     * the real items. Returns null when the asset is missing.
     */
    private static Bitmap loadCroppedPlaceholder(Context context, CosmeticItem item, String gender) {
        Bitmap raw = loadPlaceholderBitmap(context, item, gender);
        if (raw == null) {
            return null;
        }
        Bitmap cropped = cropToBackgroundSquare(raw);
        if (cropped != raw) {
            raw.recycle();
        }
        return cropped;
    }

    /**
     * Crops an opaque picture on a plain background to the square around the item.
     * The background colour is the average of the four corners; any pixel that
     * differs from it by more than {@link #PLACEHOLDER_BACKGROUND_TOLERANCE} is part
     * of the item. The square is padded by about 12% and kept inside the picture.
     * Returns the original bitmap when nothing differs from the background.
     */
    private static Bitmap cropToBackgroundSquare(Bitmap src) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (w < 16 || h < 16) {
            return src;
        }
        int[] pixels = new int[w * h];
        src.getPixels(pixels, 0, w, 0, 0, w, h);

        // Average colour of an 8x8 block in each corner.
        long sumR = 0, sumG = 0, sumB = 0;
        int count = 0;
        int[][] corners = {{0, 0}, {w - 8, 0}, {0, h - 8}, {w - 8, h - 8}};
        for (int[] c : corners) {
            for (int y = c[1]; y < c[1] + 8; y++) {
                for (int x = c[0]; x < c[0] + 8; x++) {
                    int p = pixels[y * w + x];
                    sumR += (p >> 16) & 0xFF;
                    sumG += (p >> 8) & 0xFF;
                    sumB += p & 0xFF;
                    count++;
                }
            }
        }
        int bgR = (int) (sumR / count);
        int bgG = (int) (sumG / count);
        int bgB = (int) (sumB / count);

        int minX = w, minY = h, maxX = -1, maxY = -1;
        for (int y = 0; y < h; y++) {
            int rowOffset = y * w;
            for (int x = 0; x < w; x++) {
                int p = pixels[rowOffset + x];
                int diff = Math.abs(((p >> 16) & 0xFF) - bgR)
                        + Math.abs(((p >> 8) & 0xFF) - bgG)
                        + Math.abs((p & 0xFF) - bgB);
                if (diff > PLACEHOLDER_BACKGROUND_TOLERANCE) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }
        if (maxX < minX || maxY < minY) {
            return src;
        }

        int contentW = maxX - minX + 1;
        int contentH = maxY - minY + 1;
        int side = Math.max(contentW, contentH);
        side += Math.round(side * 0.12f) * 2;

        int left = minX + contentW / 2 - side / 2;
        int top = minY + contentH / 2 - side / 2;
        if (side > w) side = w;
        if (side > h) side = h;
        if (left < 0) left = 0;
        if (top < 0) top = 0;
        if (left + side > w) left = w - side;
        if (top + side > h) top = h - side;

        return Bitmap.createBitmap(src, left, top, side, side);
    }

    /**
     * Returns a circular version of the given bitmap, for use as a round profile
     * picture. The source is centre-cropped to a square and masked to a circle;
     * the corners are transparent and the circle is filled with {@code bgColor}
     * behind the image so a transparent face render (head on empty background)
     * still reads as a solid round chip. The input bitmap is not recycled.
     *
     * @param src     any bitmap (typically a face render from {@link #compositeFace})
     * @param bgColor solid colour drawn behind the image inside the circle
     * @return a square bitmap with a circular, colour-backed region, or null when src is null
     */
    public static Bitmap toCircle(Bitmap src, int bgColor) {
        if (src == null) {
            return null;
        }
        int size = Math.min(src.getWidth(), src.getHeight());
        int left = (src.getWidth() - size) / 2;
        int top = (src.getHeight() - size) / 2;

        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);

        // Draw the filled circle (this both fills the background colour and acts as
        // the mask); then draw the face only where the circle painted (SRC_IN),
        // so anything outside the circle stays transparent.
        float radius = size / 2f;
        paint.setColor(bgColor);
        canvas.drawCircle(radius, radius, radius, paint);
        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_ATOP));
        Rect srcRect = new Rect(left, top, left + size, top + size);
        Rect dstRect = new Rect(0, 0, size, size);
        canvas.drawBitmap(src, srcRect, dstRect, paint);

        return output;
    }

    /**
     * Crops a bitmap to the square bounding box of its non-transparent pixels,
     * centred on that content. Adds a small padding margin so the item doesn't
     * touch the cell edges. Returns the original bitmap when it has no detectable
     * opaque content (fully transparent) so callers can still show something.
     */
    private static Bitmap cropToOpaqueSquare(Bitmap src) {
        if (src == null) {
            return null;
        }
        int w = src.getWidth();
        int h = src.getHeight();
        int[] pixels = new int[w * h];
        src.getPixels(pixels, 0, w, 0, 0, w, h);

        int minX = w, minY = h, maxX = -1, maxY = -1;
        final int alphaThreshold = 12; // ignore near-transparent anti-aliasing fuzz
        for (int y = 0; y < h; y++) {
            int rowOffset = y * w;
            for (int x = 0; x < w; x++) {
                int alpha = (pixels[rowOffset + x] >>> 24);
                if (alpha > alphaThreshold) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }

        // Fully transparent (no opaque pixels found) - nothing to crop to.
        if (maxX < minX || maxY < minY) {
            return src;
        }

        int contentW = maxX - minX + 1;
        int contentH = maxY - minY + 1;

        // Make the crop square (the larger of the two dimensions) so every item
        // ends up at a comparable scale and stays centred, then pad by ~12%.
        int side = Math.max(contentW, contentH);
        int padding = Math.round(side * 0.12f);
        side += padding * 2;

        int centerX = minX + contentW / 2;
        int centerY = minY + contentH / 2;
        int left = centerX - side / 2;
        int top = centerY - side / 2;

        // Clamp the square to the source bounds.
        if (side > w) side = w;
        if (side > h) side = h;
        if (left < 0) left = 0;
        if (top < 0) top = 0;
        if (left + side > w) left = w - side;
        if (top + side > h) top = h - side;

        return Bitmap.createBitmap(src, left, top, side, side);
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
