package com.RobinNotBad.BiliClient.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RadiusBackgroundSpan extends ReplacementSpan {
    private final int margin;
    private final int radius;
    private final int textColor;
    private final int bgColor;

    public RadiusBackgroundSpan(int margin, int radius, int textColor, int bgColor) {
        this.margin = margin;
        this.radius = radius;
        this.textColor = textColor;
        this.bgColor = bgColor;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
        Paint newPaint = getCustomTextPaint(paint);
        return (int) newPaint.measureText(text, start, end) + margin * 2;
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int
            bottom, @NonNull Paint paint) {
        Paint newPaint = getCustomTextPaint(paint);

        int textWidth = (int) newPaint.measureText(text, start, end);

        RectF rect = new RectF();
        rect.top = top + margin;
        rect.bottom = bottom - margin;
        rect.left = (int) (x + margin);
        rect.right = rect.left + textWidth + margin;
        paint.setColor(bgColor);
        canvas.drawRoundRect(rect, radius, radius, paint);

        newPaint.setColor(textColor);
        Paint.FontMetrics fontMetrics = newPaint.getFontMetrics();
        int offsetX = (int) ((rect.right - rect.left - textWidth) / 2) + margin;
        int offsetY = (int) ((y + fontMetrics.ascent + y + fontMetrics.descent) / 2 - (top + bottom) / 2);
        canvas.drawText(text, start, end, x + offsetX, y - offsetY, newPaint);
    }

    private TextPaint getCustomTextPaint(Paint srcPaint) {
        return new TextPaint(srcPaint);
    }
}
