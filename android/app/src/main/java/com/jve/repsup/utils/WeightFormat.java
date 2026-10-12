package com.jve.repsup.utils;

import android.content.Context;

import com.jve.repsup.R;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Formats a weight in kilograms for display: 20 becomes "20", 22.5 stays "22.5"
 * (no trailing ".0"), and {@link #kg} adds the unit ("20 kg").
 */
public final class WeightFormat {

    private WeightFormat() {}

    /** The number only, without a unit: "20" or "22.5". */
    public static String plain(double kg) {
        return BigDecimal.valueOf(kg)
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    /** The number with its unit: "20 kg". */
    public static String kg(Context context, double kg) {
        return context.getString(R.string.weight_kg_format, plain(kg));
    }
}
