package com.jve.repsup.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;

import androidx.core.content.ContextCompat;

import com.jve.repsup.R;

/**
 * A custom horizontal ruler slider view that displays tick marks with numbers.
 * Shows a center indicator line (blue) at the selected value.
 * Numbers appear below major tick marks as the user scrolls.
 */
public class RulerSliderView extends View {

    private int minValue = 0;
    private int maxValue = 300;
    private int currentValue = 70;

    // Spacing between minor ticks in pixels
    private float tickSpacing;
    // Every N minor ticks, draw a major (taller) tick with a number label
    private int majorTickInterval = 10;

    private Paint tickPaint;
    private Paint majorTickPaint;
    private Paint textPaint;
    private Paint indicatorPaint;

    private float scrollOffset = 0f;
    private float density;

    private OverScroller scroller;
    private GestureDetector gestureDetector;
    private OnValueChangedListener listener;

    public interface OnValueChangedListener {
        void onValueChanged(int value);
    }

    public RulerSliderView(Context context) {
        super(context);
        init(context);
    }

    public RulerSliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RulerSliderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        density = context.getResources().getDisplayMetrics().density;
        tickSpacing = 12 * density; // 12dp between each tick

        // Minor tick paint (grey, shorter)
        tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setColor(ContextCompat.getColor(context, R.color.muted));
        tickPaint.setStrokeWidth(1.5f * density);
        tickPaint.setStrokeCap(Paint.Cap.ROUND);

        // Major tick paint (white, taller)
        majorTickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        majorTickPaint.setColor(ContextCompat.getColor(context, R.color.white));
        majorTickPaint.setStrokeWidth(2f * density);
        majorTickPaint.setStrokeCap(Paint.Cap.ROUND);

        // Number text paint
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(ContextCompat.getColor(context, R.color.muted));
        textPaint.setTextSize(12 * density);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // Center indicator paint (blue line)
        indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorPaint.setColor(ContextCompat.getColor(context, R.color.light_blue));
        indicatorPaint.setStrokeWidth(3f * density);
        indicatorPaint.setStrokeCap(Paint.Cap.ROUND);

        scroller = new OverScroller(context);
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                scrollOffset += distanceX;
                clampScrollOffset();
                updateCurrentValue();
                invalidate();
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                int maxScroll = (int) ((maxValue - minValue) * tickSpacing);
                scroller.fling((int) scrollOffset, 0, (int) -velocityX, 0,
                        0, maxScroll, 0, 0);
                invalidate();
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                scroller.forceFinished(true);
                return true;
            }
        });
    }

    public void setRange(int min, int max) {
        this.minValue = min;
        this.maxValue = max;
        clampScrollOffset();
        invalidate();
    }

    public void setMajorTickInterval(int interval) {
        this.majorTickInterval = interval;
        invalidate();
    }

    public void setValue(int value) {
        this.currentValue = Math.max(minValue, Math.min(maxValue, value));
        scrollOffset = (currentValue - minValue) * tickSpacing;
        invalidate();
    }

    public int getValue() {
        return currentValue;
    }

    public void setOnValueChangedListener(OnValueChangedListener listener) {
        this.listener = listener;
    }

    private void clampScrollOffset() {
        float maxScroll = (maxValue - minValue) * tickSpacing;
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
    }

    private void updateCurrentValue() {
        int newValue = minValue + Math.round(scrollOffset / tickSpacing);
        newValue = Math.max(minValue, Math.min(maxValue, newValue));
        if (newValue != currentValue) {
            currentValue = newValue;
            if (listener != null) {
                listener.onValueChanged(currentValue);
            }
        }
    }

    /**
     * Snaps to the nearest integer value.
     */
    private void snapToValue() {
        float targetOffset = (currentValue - minValue) * tickSpacing;
        scrollOffset = targetOffset;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = gestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP ||
                event.getAction() == MotionEvent.ACTION_CANCEL) {
            snapToValue();
        }
        return handled || super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollOffset = scroller.getCurrX();
            clampScrollOffset();
            updateCurrentValue();
            invalidate();
        } else if (!scroller.isFinished()) {
            snapToValue();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        float centerX = width / 2f;

        // The top area is for tick marks, bottom area for numbers
        float tickTop = 8 * density;
        float minorTickHeight = 20 * density;
        float majorTickHeight = 32 * density;
        float textY = height - 4 * density;

        // Calculate visible range
        float startOffset = scrollOffset - centerX;
        int startTick = (int) Math.floor(startOffset / tickSpacing);
        int endTick = (int) Math.ceil((startOffset + width) / tickSpacing);

        startTick = Math.max(0, startTick);
        endTick = Math.min(maxValue - minValue, endTick);

        for (int i = startTick; i <= endTick; i++) {
            float x = centerX - scrollOffset + (i * tickSpacing);
            int value = minValue + i;

            if (value % majorTickInterval == 0) {
                // Major tick: taller + number label
                canvas.drawLine(x, tickTop, x, tickTop + majorTickHeight, majorTickPaint);
                // Draw number below
                String label = String.valueOf(value);
                canvas.drawText(label, x, textY, textPaint);
            } else {
                // Minor tick: shorter
                canvas.drawLine(x, tickTop, x, tickTop + minorTickHeight, tickPaint);
            }
        }

        // Draw center indicator line (blue, full height of tick area)
        canvas.drawLine(centerX, 0, centerX, tickTop + majorTickHeight + 4 * density, indicatorPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredHeight = (int) (70 * density); // 70dp total height
        int height = resolveSize(desiredHeight, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }
}
