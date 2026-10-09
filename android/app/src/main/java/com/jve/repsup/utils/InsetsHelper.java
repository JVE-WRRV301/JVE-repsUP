package com.jve.repsup.utils;

import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Shared window-inset helpers so every screen handles edge-to-edge display and
 * the on-screen keyboard the same way.
 *
 * <p>With targetSdk 36, Android 15+ phones force edge-to-edge: the app draws
 * under the status and navigation bars, and {@code adjustResize} alone no longer
 * pushes content above the keyboard. These helpers apply the system-bar and IME
 * (keyboard) insets so content stays clear of the system UI.
 *
 * <p>The keyboard inset is applied as the scroll container's <b>bottom margin</b>,
 * not padding. Bottom padding leaves the scroll view the same height, so its
 * built-in "keep the focused child visible on resize" never runs and a low field
 * stays hidden behind the keyboard. A bottom margin actually shrinks the scroll
 * view above the keyboard, the way {@code adjustResize} used to, so the focused
 * field is scrolled into view automatically. As a safety net we also request that
 * the focused view be scrolled on screen once the inset settles.
 */
public final class InsetsHelper {

    private InsetsHelper() {
        // Utility class — no instances.
    }

    /**
     * Opts the window into drawing behind the system bars (edge-to-edge). Use on
     * screens that want a full-screen background image behind the status/nav bars.
     * This replaces the deprecated {@code FLAG_LAYOUT_NO_LIMITS} flag.
     */
    public static void enableEdgeToEdge(Window window) {
        WindowCompat.setDecorFitsSystemWindows(window, false);
    }

    /**
     * Shrinks a scrolling container above the keyboard by applying the IME inset
     * as the container's bottom margin, so the scroll view resizes and brings the
     * focused field into view. When the keyboard is closed the margin returns to
     * its original value, keeping any existing spacing (e.g. clearance for the
     * gesture bar) intact.
     *
     * @param scrollContainer a scrolling view (ScrollView, NestedScrollView or
     *                        RecyclerView) whose layout params are
     *                        {@link ViewGroup.MarginLayoutParams}.
     */
    public static void applyImeBottomPadding(final View scrollContainer) {
        final int baseMarginBottom = marginBottomOf(scrollContainer);
        ViewCompat.setOnApplyWindowInsetsListener(scrollContainer, (v, insets) -> {
            int imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            boolean imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            setMarginBottom(v, baseMarginBottom + imeBottom);
            if (imeVisible) {
                scrollFocusedChildIntoView(v);
            }
            return insets;
        });
    }

    /**
     * Applies the status/navigation bar insets as top and bottom padding so the
     * view's content never sits under the clock or the gesture bar. Preserves the
     * view's original paddings and adds the system-bar insets on top.
     */
    public static void applySystemBarsPadding(final View view) {
        final int basePaddingTop = view.getPaddingTop();
        final int basePaddingBottom = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), basePaddingTop + bars.top,
                    v.getPaddingRight(), basePaddingBottom + bars.bottom);
            return insets;
        });
    }

    /**
     * Handles both the system-bar insets and the keyboard on a single scrolling
     * container that sits edge-to-edge. The status/nav bars are applied as
     * top/bottom padding (so the background still draws behind them and content
     * clears the gesture bar when the keyboard is closed); the keyboard inset is
     * applied as bottom margin so the scroll view shrinks above the keyboard and
     * scrolls the focused field into view.
     *
     * @param scrollContainer a scrolling view whose layout params are
     *                        {@link ViewGroup.MarginLayoutParams}.
     */
    public static void applySystemBarsAndImeBottomPadding(final View scrollContainer) {
        final int basePaddingTop = scrollContainer.getPaddingTop();
        final int basePaddingBottom = scrollContainer.getPaddingBottom();
        final int baseMarginBottom = marginBottomOf(scrollContainer);
        ViewCompat.setOnApplyWindowInsetsListener(scrollContainer, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            boolean imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            v.setPadding(v.getPaddingLeft(), basePaddingTop + bars.top,
                    v.getPaddingRight(), basePaddingBottom + bars.bottom);
            // Lift the scroll view above the keyboard. The IME inset already
            // includes the nav-bar height, so no extra bottom spacing is needed
            // while the keyboard is open.
            setMarginBottom(v, baseMarginBottom + imeBottom);
            if (imeVisible) {
                scrollFocusedChildIntoView(v);
            }
            return insets;
        });
    }

    /**
     * Variant for a scroll container that keeps {@code fitsSystemWindows="true"}
     * for its status/nav bar handling. Setting an inset listener on the scroll
     * view itself would cancel {@code fitsSystemWindows}, so this listens on the
     * scroll view's parent (which consumes nothing and passes insets through) and
     * adjusts the scroll view's bottom margin to shrink it above the keyboard.
     *
     * @param scrollContainer a scroll view whose parent is a ViewGroup and whose
     *                        own layout params are
     *                        {@link ViewGroup.MarginLayoutParams}.
     */
    public static void applyImeBottomMarginViaParent(final View scrollContainer) {
        final int baseMarginBottom = marginBottomOf(scrollContainer);
        View parent = (View) scrollContainer.getParent();
        if (parent == null) {
            // Fall back to the direct approach if there's no parent yet.
            applyImeBottomPadding(scrollContainer);
            return;
        }
        ViewCompat.setOnApplyWindowInsetsListener(parent, (v, insets) -> {
            int imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            boolean imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            setMarginBottom(scrollContainer, baseMarginBottom + imeBottom);
            if (imeVisible) {
                scrollFocusedChildIntoView(scrollContainer);
            }
            return insets;
        });
    }

    /** Reads a view's current bottom margin, or 0 if it has no margin params. */
    private static int marginBottomOf(View view) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            return ((ViewGroup.MarginLayoutParams) lp).bottomMargin;
        }
        return 0;
    }

    /** Sets a view's bottom margin, only re-laying out when the value changes. */
    private static void setMarginBottom(View view, int bottomMargin) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
            if (mlp.bottomMargin != bottomMargin) {
                mlp.bottomMargin = bottomMargin;
                view.setLayoutParams(mlp);
            }
        }
    }

    /** Gap kept between the bottom of the focused field and the top of the keyboard. */
    private static final int FOCUS_GAP_DP = 16;

    /**
     * Safety net: after the layout settles with the keyboard showing, ask the
     * currently focused view to scroll itself fully on screen. The margin change
     * usually handles this, but posting an explicit request covers cases where the
     * resize alone doesn't bring a tall or low field into view. The requested
     * rectangle extends {@value #FOCUS_GAP_DP}dp below the field so it isn't left
     * flush against the top of the keyboard.
     */
    private static void scrollFocusedChildIntoView(final View scrollContainer) {
        scrollContainer.post(() -> {
            View focused = scrollContainer.findFocus();
            if (focused != null) {
                int gapPx = Math.round(FOCUS_GAP_DP
                        * focused.getResources().getDisplayMetrics().density);
                focused.requestRectangleOnScreen(
                        new android.graphics.Rect(0, 0,
                                focused.getWidth(), focused.getHeight() + gapPx),
                        false);
            }
        });
    }
}
