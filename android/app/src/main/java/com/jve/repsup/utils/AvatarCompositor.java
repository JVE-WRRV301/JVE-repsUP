package com.jve.repsup.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.jve.repsup.R;
import com.jve.repsup.models.Avatar;
import com.jve.repsup.models.OutfitSlot;

/**
 * Composites avatar layers (body + outfit) into a single Bitmap.
 * Uses Android Canvas to draw base body, then overlays for top, bottom, shoes, and accessories.
 * Falls back to a default placeholder when specific assets are not yet available.
 */
public class AvatarCompositor {

    private static final String TAG = "AvatarCompositor";
    private static final int AVATAR_SIZE = 512; // px

    /**
     * Composites avatar layers into a single Bitmap.
     * Layers: base body → top → bottom → shoes → accessories
     *
     * @param context Application or Activity context
     * @param avatar  The user's Avatar record (contains bodyShape info)
     * @param outfit  The user's equipped outfit slots
     * @return A composited Bitmap of the full avatar
     */
    public static Bitmap composite(Context context, Avatar avatar, OutfitSlot outfit) {
        Bitmap result = Bitmap.createBitmap(AVATAR_SIZE, AVATAR_SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        // Layer 1: Base body
        int bodyRes = getBaseBodyResource(avatar.getSkinTone(), avatar.getBodyShape());
        drawLayer(context, canvas, bodyRes);

        // Layer 2: Top
        if (outfit != null && outfit.getTopId() != null) {
            int topRes = getDefaultOutfitResource("top");
            drawLayer(context, canvas, topRes);
        }

        // Layer 3: Bottom
        if (outfit != null && outfit.getBottomId() != null) {
            int bottomRes = getDefaultOutfitResource("bottom");
            drawLayer(context, canvas, bottomRes);
        }

        // Layer 4: Shoes
        if (outfit != null && outfit.getShoesId() != null) {
            int shoesRes = getDefaultOutfitResource("shoes");
            drawLayer(context, canvas, shoesRes);
        }

        // Layer 5: Accessories (none by default)
        if (outfit != null && outfit.getAccessoriesId() != null) {
            // Future: resolve accessory resource and draw
            Log.d(TAG, "Accessories slot filled but no asset mapping yet");
        }

        return result;
    }

    /**
     * Returns the resource ID for the base body image given gender and body type.
     * Since real PNG assets don't exist yet, all combinations fall back to the default placeholder.
     * The structure is ready for real assets — just add drawables matching the naming pattern:
     * ic_avatar_body_{gender}_{bodyType} (e.g., ic_avatar_body_male_mesomorph)
     *
     * @param gender   "male" or "female"
     * @param bodyType "ectomorph", "mesomorph", "endomorph", "athletic", "stocky", or "slim"
     * @return Drawable resource ID for the base body
     */
    public static int getBaseBodyResource(String gender, String bodyType) {
        // Attempt to resolve a specific drawable for the gender × bodyType combination.
        // When real assets are added, they should follow the naming convention:
        // ic_avatar_body_male_ectomorph, ic_avatar_body_female_mesomorph, etc.
        // For now, all combinations resolve to the default placeholder.

        if (gender == null || bodyType == null) {
            return R.drawable.ic_avatar_body_default;
        }

        // Normalize inputs
        String g = gender.toLowerCase().trim();
        String bt = bodyType.toLowerCase().trim();

        // Map to resource name pattern: ic_avatar_body_{gender}_{bodytype}
        // In the future, real assets will be resolved here.
        // For now, return the universal default.
        switch (g + "_" + bt) {
            case "male_ectomorph":
            case "male_mesomorph":
            case "male_endomorph":
            case "male_athletic":
            case "male_stocky":
            case "male_slim":
            case "female_ectomorph":
            case "female_mesomorph":
            case "female_endomorph":
            case "female_athletic":
            case "female_stocky":
            case "female_slim":
                // When assets are added, each case returns its own resource.
                // e.g., return R.drawable.ic_avatar_body_male_mesomorph;
                return R.drawable.ic_avatar_body_default;
            default:
                return R.drawable.ic_avatar_body_default;
        }
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
