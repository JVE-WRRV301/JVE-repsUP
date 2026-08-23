package com.jve.repsup.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.jve.repsup.R;

/**
 * A vertical number picker view that displays ages with the selected value
 * centered, larger, and highlighted in blue with horizontal separator lines.
 * Numbers fade out as they get further from the center.
 */
public class AgePickerView extends ScrollView {

    private static final int MIN_AGE = 13;
    private static final int MAX_AGE = 99;
    private static final int VISIBLE_ITEMS = 7; // Number of items visible at once
    private static final int ITEM_HEIGHT_DP = 52;

    private LinearLayout container;
    private int selectedAge = 18;
    private int itemHeightPx;
    private Paint linePaint;
    private OnAgeSelectedListener listener;

    public interface OnAgeSelectedListener {
        void onAgeSelected(int age);
    }

    public AgePickerView(Context context) {
        super(context);
        init(context);
    }

    public AgePickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AgePickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setVerticalScrollBarEnabled(false);
        setOverScrollMode(OVER_SCROLL_NEVER);

        float density = context.getResources().getDisplayMetrics().density;
        itemHeightPx = (int) (ITEM_HEIGHT_DP * density);

        linePaint = new Paint();
        linePaint.setColor(ContextCompat.getColor(context, R.color.light_blue));
        linePaint.setStrokeWidth(2 * density);

        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER_HORIZONTAL);
        addView(container);

        buildItems(context);
    }

    private void buildItems(Context context) {
        container.removeAllViews();

        // Add top padding items (empty space for centering)
        int paddingItems = VISIBLE_ITEMS / 2;
        for (int i = 0; i < paddingItems; i++) {
            container.addView(createEmptyItem(context));
        }

        // Add age items
        for (int age = MIN_AGE; age <= MAX_AGE; age++) {
            container.addView(createAgeItem(context, age));
        }

        // Add bottom padding items
        for (int i = 0; i < paddingItems; i++) {
            container.addView(createEmptyItem(context));
        }
    }

    private View createEmptyItem(Context context) {
        TextView tv = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, itemHeightPx);
        tv.setLayoutParams(params);
        return tv;
    }

    private TextView createAgeItem(Context context, int age) {
        TextView tv = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, itemHeightPx);
        tv.setLayoutParams(params);
        tv.setText(String.valueOf(age));
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(ContextCompat.getColor(context, R.color.white));
        tv.setTextSize(20);
        tv.setTag(age);
        return tv;
    }

    public void setOnAgeSelectedListener(OnAgeSelectedListener listener) {
        this.listener = listener;
    }

    public void setSelectedAge(int age) {
        this.selectedAge = Math.max(MIN_AGE, Math.min(MAX_AGE, age));
        post(() -> {
            int targetScroll = (selectedAge - MIN_AGE) * itemHeightPx;
            scrollTo(0, targetScroll);
            updateItemAppearances();
        });
    }

    public int getSelectedAge() {
        return selectedAge;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        // Calculate selected age from scroll position
        int centerOffset = t + (VISIBLE_ITEMS / 2) * itemHeightPx;
        int paddingItems = VISIBLE_ITEMS / 2;
        int ageIndex = (centerOffset / itemHeightPx) - paddingItems;
        int newAge = MIN_AGE + Math.max(0, Math.min(MAX_AGE - MIN_AGE, ageIndex));

        if (newAge != selectedAge) {
            selectedAge = newAge;
            if (listener != null) {
                listener.onAgeSelected(selectedAge);
            }
        }
        updateItemAppearances();
    }

    @Override
    public void fling(int velocityY) {
        super.fling(velocityY / 2); // Slow down fling for better control
    }

    /**
     * Snaps to the nearest age when scrolling stops.
     */
    private void snapToAge() {
        int targetScroll = (selectedAge - MIN_AGE) * itemHeightPx;
        smoothScrollTo(0, targetScroll);
    }

    private final Runnable snapRunnable = this::snapToAge;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Re-scroll to selected after layout
        post(() -> setSelectedAge(selectedAge));
    }

    /**
     * Updates text sizes and alpha for items based on distance from center.
     */
    private void updateItemAppearances() {
        int paddingItems = VISIBLE_ITEMS / 2;
        int scrollY = getScrollY();
        int centerY = scrollY + (getHeight() / 2);

        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (!(child instanceof TextView) || child.getTag() == null) continue;

            TextView tv = (TextView) child;
            int childCenterY = child.getTop() + (itemHeightPx / 2);
            int distanceFromCenter = Math.abs(centerY - childCenterY);
            float maxDistance = (VISIBLE_ITEMS / 2f) * itemHeightPx;

            float ratio = Math.min(1f, distanceFromCenter / maxDistance);

            // Scale text size: center = 42sp, edges = 16sp
            float textSize = 42f - (ratio * 26f);
            tv.setTextSize(textSize);

            // Alpha: center = 1.0, edges = 0.3
            float alpha = 1.0f - (ratio * 0.7f);
            tv.setAlpha(alpha);

            // Color: center item is light_blue, others are white
            if (distanceFromCenter < itemHeightPx / 2) {
                tv.setTextColor(ContextCompat.getColor(getContext(), R.color.light_blue));
            } else {
                tv.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        // Draw horizontal lines above and below the center item
        int centerY = getScrollY() + (getHeight() / 2);
        int halfItem = itemHeightPx / 2;
        int lineTopY = centerY - halfItem;
        int lineBottomY = centerY + halfItem;
        int paddingH = (int) (24 * getResources().getDisplayMetrics().density);

        canvas.drawLine(paddingH, lineTopY, getWidth() - paddingH, lineTopY, linePaint);
        canvas.drawLine(paddingH, lineBottomY, getWidth() - paddingH, lineBottomY, linePaint);
    }

    /**
     * When touch ends, snap to nearest item.
     */
    @Override
    public boolean onTouchEvent(android.view.MotionEvent ev) {
        boolean result = super.onTouchEvent(ev);
        if (ev.getAction() == android.view.MotionEvent.ACTION_UP ||
                ev.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
            removeCallbacks(snapRunnable);
            postDelayed(snapRunnable, 100);
        }
        return result;
    }
}
