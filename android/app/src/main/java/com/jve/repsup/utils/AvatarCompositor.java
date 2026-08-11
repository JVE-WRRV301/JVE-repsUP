package com.jve.repsup.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.jve.repsup.models.Avatar;
import com.jve.repsup.models.CosmeticItem;
import com.jve.repsup.models.OutfitSlot;

import java.util.List;

/**
 * Composites avatar layers into a single Bitmap.
 * Layers: base body → top → bottom → shoes → accessories.
 *
 * Currently renders a placeholder avatar silhouette. When real cosmetic
 * image assets are available, this class will layer them via Canvas.
 */
public class AvatarCompositor {

    private static final int AVATAR_WIDTH = 256;
    private static final int AVATAR_HEIGHT = 384;

    /**
     * Composites the avatar with the given outfit into a Bitmap.
     * Currently draws a stylised placeholder silhouette with colour accents
     * based on equipped cosmetic types.
     */
    public static Bitmap composite(Context context, Avatar avatar, OutfitSlot outfit,
                                   List<CosmeticItem> equippedItems) {
        Bitmap bitmap = Bitmap.createBitmap(AVATAR_WIDTH, AVATAR_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw base body silhouette
        Paint bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bodyPaint.setColor(getSkinColor(avatar != null ? avatar.getSkinTone() : null));

        // Head
        canvas.drawCircle(AVATAR_WIDTH / 2f, 60, 40, bodyPaint);
        // Body
        canvas.drawRoundRect(new RectF(80, 100, 176, 280), 16, 16, bodyPaint);
        // Legs
        canvas.drawRoundRect(new RectF(88, 280, 120, 370), 8, 8, bodyPaint);
        canvas.drawRoundRect(new RectF(136, 280, 168, 370), 8, 8, bodyPaint);

        // Overlay equipped items as coloured accents
        if (equippedItems != null) {
            for (CosmeticItem item : equippedItems) {
                drawCosmeticLayer(canvas, item);
            }
        }

        return bitmap;
    }

    /**
     * Overload for simple composite without equipped item list.
     */
    public static Bitmap composite(Context context, Avatar avatar, OutfitSlot outfit) {
        return composite(context, avatar, outfit, null);
    }

    private static void drawCosmeticLayer(Canvas canvas, CosmeticItem item) {
        if (item == null) return;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(getCosmeticColor(item.getType()));

        switch (item.getType()) {
            case "top":
                canvas.drawRoundRect(new RectF(82, 102, 174, 200), 12, 12, paint);
                break;
            case "bottom":
                canvas.drawRoundRect(new RectF(86, 200, 170, 282), 8, 8, paint);
                break;
            case "shoes":
                canvas.drawRoundRect(new RectF(84, 356, 124, 374), 6, 6, paint);
                canvas.drawRoundRect(new RectF(132, 356, 172, 374), 6, 6, paint);
                break;
            case "hair":
                canvas.drawRoundRect(new RectF(80, 24, 176, 70), 20, 20, paint);
                break;
            case "face":
            case "skin":
                // Tints the head area
                paint.setAlpha(80);
                canvas.drawCircle(AVATAR_WIDTH / 2f, 60, 40, paint);
                break;
        }
    }

    private static int getSkinColor(String skinTone) {
        if (skinTone == null) return Color.parseColor("#D2A679");
        switch (skinTone.toLowerCase()) {
            case "light": return Color.parseColor("#FDDBB4");
            case "medium": return Color.parseColor("#D2A679");
            case "dark": return Color.parseColor("#8D5524");
            default: return Color.parseColor("#D2A679");
        }
    }

    private static int getCosmeticColor(String type) {
        switch (type) {
            case "top": return Color.parseColor("#4A90D9");
            case "bottom": return Color.parseColor("#2D5A8A");
            case "shoes": return Color.parseColor("#3D3D3D");
            case "hair": return Color.parseColor("#4A3728");
            case "face":
            case "skin": return Color.parseColor("#FFB6C1");
            default: return Color.GRAY;
        }
    }
}
